package com.example.serasa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class SerasaService {

    public boolean consultarCpfSerasa(String cpf) {
        return CPFS_NEGATIVADOS.contains(cpf);
    }

    private static final Set<String> CPFS_NEGATIVADOS = Set.of(
            "18142226006",
            "16470435068"
    );

}