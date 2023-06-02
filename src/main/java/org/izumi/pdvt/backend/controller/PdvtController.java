package org.izumi.pdvt.backend.controller;

import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.function.BiConsumer;

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
import org.izumi.pdvt.backend.entity.StoredFile;
import org.izumi.pdvt.backend.entity.PdvtFile;
import org.izumi.pdvt.backend.repository.ClientRepository;
import org.izumi.pdvt.backend.repository.PdvtFileRepository;
import org.izumi.pdvt.backend.service.ClientService;
import org.izumi.pdvt.backend.utils.Utils;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@RestController
public class PdvtController extends AbstractController {
    private final SystemAuthenticator authenticator;
    private final ClientRepository clientRepository;
    private final PdvtFileRepository pdvtFileRepository;
    private final FileStorageLocator locator;
    private final Metadata metadata;
    private final DataManager dataManager;
    private final SimpMessagingTemplate template;
    private final ClientService clientService;

    @PostMapping("/client/{code}/create-if-absent")
    public ResponseEntity<Client> createClientIfAbsent(@PathVariable(name = "code") String codeword,
                                                       @RequestBody String data) {
        return authenticator.withSystem(() -> {
            final Code code = new Code(data);
            final Optional<Client> existingOptional = clientRepository.findByCode(codeword);
            if (existingOptional.isEmpty()) {
                return ok(clientService.create(code));
            } else {
                final Client existing = existingOptional.get();
                if (!clientService.checkPassword(existing, code)) {
                    return forbidden();
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
                if (!clientService.checkPassword(client, code)) {
                    return forbidden();
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
                if (!clientService.checkPassword(client, code)) {
                    return forbidden();
                }

                dataManager.remove(client);
                return ok();
            }
        });
    }

    @PostMapping("/client/{code}/files/add")
    public ResponseEntity<?> upload(@PathVariable(name = "code") String code,
                                    @RequestParam("code") String data,
                                    @RequestParam("file") MultipartFile file) {
        return upload(code, new Code(data), file, this::emitFile);
    }

    @PostMapping("/client/{code}/files/add-and-analyze")
    public ResponseEntity<?> uploadAndAnalyze(@PathVariable(name = "code") String code,
                                              @RequestParam("code") String data,
                                              @RequestParam("file") MultipartFile file) {
        return upload(code, new Code(data), file, this::emitFileAndAnalyze);
    }

    private ResponseEntity<?> upload(String code,
                                     Code c,
                                     MultipartFile file,
                                     BiConsumer<PdvtFile, InputStream> emitFunction) {
        return authenticator.withSystem(() -> {
            final Client client = clientRepository.findByCode(code).orElseThrow();
            if (!clientService.checkPassword(client, c)) {
                return forbidden();
            }

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
            pdvtFile.setFile(storedFileOf(ref));
            final PdvtFile saved = dataManager.save(new SaveContext()
                    .saving(pdvtFile)
                    .saving(pdvtFile.getFile())
            ).get(pdvtFile);

            emitFunction.accept(saved, storage.openStream(ref));
            storage.removeFile(ref);

            return ok();
        });
    }

    private void emitFile(PdvtFile file, InputStream stream) {
        final String code = file.getClient().getCode();
        final String filename = file.getFile().getName();
        final String body = Utils.silently(() -> Base64.getEncoder().encodeToString(stream.readAllBytes()));
        final String payload = filename.length() + "." + filename + body;

        template.convertAndSendToUser(code, "/ws/pdvt", payload);
    }

    private void emitFileAndAnalyze(PdvtFile file, InputStream stream) {
        final String code = file.getClient().getCode();
        final String filename = file.getFile().getName();
        final String body = Utils.silently(() -> Base64.getEncoder().encodeToString(stream.readAllBytes()));
        final String payload = filename.length() + "." + filename + body;

        template.convertAndSendToUser(code, "/ws/file-and-analyze", payload);
    }

    private void delete(PdvtFile pdvtFile) {
        dataManager.save(new SaveContext()
                .removing(pdvtFile)
                .removing(pdvtFile.getFile())
        );
    }

    private StoredFile storedFileOf(FileRef ref) {
        final StoredFile dto = metadata.create(StoredFile.class);
        dto.setName(ref.getFileName());
        dto.setPath(ref.getPath());

        return dto;
    }

    private String filename(MultipartFile file) {
        return Optional.ofNullable(file.getOriginalFilename())
                .orElseGet(file::getName);
    }
}
