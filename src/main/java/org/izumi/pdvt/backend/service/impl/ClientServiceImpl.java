package org.izumi.pdvt.backend.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.izumi.pdvt.backend.Code;
import org.izumi.pdvt.backend.entity.Client;
import org.izumi.pdvt.backend.repository.ClientRepository;
import org.izumi.pdvt.backend.service.ClientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ClientServiceImpl implements ClientService {
    private final Metadata metadata;
    private final PasswordEncoder encoder;
    private final ClientRepository clientRepository;
    private final DataManager dataManager;

    @Override
    public boolean checkPassword(Supplier<Client> client, Code request) {
        return checkPassword(client.get(), request);
    }

    @Override
    public boolean checkPassword(Client client, Code request) {
        final String password = client.getPassword();
        if (Objects.isNull(password) || password.isBlank()) {
            return !request.hasPassword();
        } else {
            final boolean matches = encoder.matches(request.getPassword(), password);
            if (!matches) {
                return false;
            }

            client.setLastActivity(LocalDateTime.now());
            dataManager.save(client);
            return true;
        }
    }

    @Override
    public Client create(Code code) {
        final Client client = metadata.create(Client.class);
        client.setCode(code.getCodeword());
        if (code.hasPassword()) {
            client.setPassword(encoder.encode(code.getPassword()));
        }
        client.setLastActivity(LocalDateTime.now());

        return clientRepository.save(client);
    }
}
