package org.izumi.pdvt.backend.service;

import java.util.function.Supplier;

import org.izumi.pdvt.backend.Code;
import org.izumi.pdvt.backend.entity.Client;

public interface ClientService {
    boolean checkPassword(Supplier<Client> client, Code request);
    boolean checkPassword(Client client, Code request);
    Client create(Code code);
}
