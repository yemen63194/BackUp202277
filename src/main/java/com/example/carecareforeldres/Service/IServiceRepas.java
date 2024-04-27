package com.example.carecareforeldres.Service;

import com.example.carecareforeldres.DTO.RepasAvecPlatsDTO;
import com.example.carecareforeldres.Entity.Repas;

import java.util.List;

public interface IServiceRepas {
    Repas addPlat(Repas pt);

    List<Repas> getAll();

    void remove(int idf);

    Repas update(Repas res);


    Repas addRepasAvecPlats(RepasAvecPlatsDTO repasDTO);

    Repas AffecterRepasAUser(RepasAvecPlatsDTO repasDTO, Integer idUser);
}
