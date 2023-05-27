package org.izumi.pdvt.backend.controller;

import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import io.jmix.core.CorsProperties;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.security.SystemAuthenticator;
import lombok.RequiredArgsConstructor;
import org.izumi.pdvt.backend.Code;
import org.izumi.pdvt.backend.entity.Client;
import org.izumi.pdvt.backend.dto.FileDto;
import org.izumi.pdvt.backend.entity.PdvtFile;
import org.izumi.pdvt.backend.repository.ClientRepository;
import org.izumi.pdvt.backend.repository.PdvtFileRepository;
import org.izumi.pdvt.backend.utils.Utils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class PdvtController {
    private final CorsProperties corsProperties;
    private final PasswordEncoder encoder;
    private final SystemAuthenticator authenticator;
    private final ClientRepository clientRepository;
    private final PdvtFileRepository pdvtFileRepository;
    private final FileStorageLocator locator;
    private final Metadata metadata;
    private final DataManager dataManager;
    private final SimpMessagingTemplate template;

    @PostMapping("/client/{code}/create-if-absent")
    public ResponseEntity<Client> createClientIfAbsent(@PathVariable(name = "code") String codeword,
                                                       @RequestBody String data) {
        return authenticator.withSystem(() -> {
            final Code code = new Code(data);
            final Optional<Client> existingOptional = clientRepository.findByCode(codeword);
            if (existingOptional.isEmpty()) {
                final Client client = metadata.create(Client.class);
                client.setCode(codeword);
                client.setPassword(encoder.encode(code.getPassword()));
                final Client saved = clientRepository.save(client);
                return ok(saved);
            } else {
                final Client existing = existingOptional.get();
                if (!passPasswordsCheck(existing, code)) {
                    return badRequest();
                }

                return ok(existing);
            }
        });
    }

    @PostMapping("/client/{code}/check-exists")
    public ResponseEntity<Boolean> checkExists(@PathVariable(name = "code") String codeword, @RequestBody String data) {
        return authenticator.withSystem(() -> {
            final Code code = new Code(data);
            final Optional<Client> existingOptional = clientRepository.findByCode(codeword);
            if (existingOptional.isEmpty()) {
                return ok(false);
            } else {
                final Client client = existingOptional.get();
                if (!passPasswordsCheck(client, code)) {
                    return badRequest();
                }

                return ok(true);
            }
        });
    }

    @PostMapping("/client/{code}/delete")
    public ResponseEntity<?> delete(@PathVariable(name = "code") String codeword, @RequestBody String data) {
        return authenticator.withSystem(() -> {
            final Code code = new Code(data);
            final Optional<Client> existingOptional = clientRepository.findByCode(codeword);
            if (existingOptional.isEmpty()) {
                return badRequest();
            } else {
                final Client client = existingOptional.get();
                if (!passPasswordsCheck(client, code)) {
                    return forbidden();
                }

                dataManager.remove(client);
                return ok();
            }
        });
    }

    @PostMapping("/client/{code}/files/add")
    public ResponseEntity<PdvtFile> upload(@PathVariable(name = "code") String code,
                                           @RequestParam("file") MultipartFile file) {

        return authenticator.withSystem(() -> {
            final Client client = clientRepository.findByCode(code).orElseThrow();
            final FileStorage storage = client.getStorage(locator);

            final String filename = filename(file);
            final Optional<PdvtFile> optionalFile = pdvtFileRepository.findByClientAndFileDtoName(
                    builder -> builder.addFetchPlan(FetchPlan.BASE).add("file", FetchPlan.BASE),
                    client,
                    filename
            );
            optionalFile.ifPresent(this::delete);

            final FileRef ref = Utils.silently(() -> storage.saveStream(filename, file.getInputStream()));
            final PdvtFile pdvtFile = metadata.create(PdvtFile.class);
            pdvtFile.setClient(client);
            pdvtFile.setFile(fileDtoOf(ref));
            final PdvtFile saved = dataManager.save(new SaveContext()
                    .saving(pdvtFile)
                    .saving(pdvtFile.getFile())
            ).get(pdvtFile);

            emit(saved, storage.openStream(ref));

            return ResponseEntity.ok()
                    .body(saved);
        });
    }

    private void emit(PdvtFile file, InputStream stream) {
        final String code = file.getClient().getCode();
        final String filename = file.getFile().getName();
        final String body = Utils.silently(() -> Base64.getEncoder().encodeToString(stream.readAllBytes()));
        final String payload = filename.length() + "." + filename + body;

        template.convertAndSendToUser(code, "/ws/pdvt", payload);
    }

    private void delete(PdvtFile pdvtFile) {
        dataManager.save(new SaveContext()
                .removing(pdvtFile)
                .removing(pdvtFile.getFile())
        );
    }

    private FileDto fileDtoOf(FileRef ref) {
        final FileDto dto = metadata.create(FileDto.class);
        dto.setName(ref.getFileName());
        dto.setPath(ref.getPath());

        return dto;
    }

    private String filename(MultipartFile file) {
        return Optional.ofNullable(file.getOriginalFilename())
                .orElseGet(file::getName);
    }

    private boolean passPasswordsCheck(Client client, Code request) {
        final String password = client.getPassword();
        if (Objects.isNull(password) || password.isBlank()) {
            return request.getPassword().isBlank();
        } else {
            return encoder.matches(request.getPassword(), password);
        }
    }

    private <T> ResponseEntity<T> ok() {
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, String.join(", ", corsProperties.getAllowedOrigins()))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", corsProperties.getAllowedMethods()))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", corsProperties.getAllowedHeaders()))
                .build();
    }

    private <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, String.join(", ", corsProperties.getAllowedOrigins()))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", corsProperties.getAllowedMethods()))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", corsProperties.getAllowedHeaders()))
                .body(body);
    }

    private <T> ResponseEntity<T> badRequest() {
        return ResponseEntity.badRequest()
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, String.join(", ", corsProperties.getAllowedOrigins()))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", corsProperties.getAllowedMethods()))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", corsProperties.getAllowedHeaders()))
                .build();
    }

    private <T> ResponseEntity<T> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, String.join(", ", corsProperties.getAllowedOrigins()))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", corsProperties.getAllowedMethods()))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", corsProperties.getAllowedHeaders()))
                .build();
    }
}
