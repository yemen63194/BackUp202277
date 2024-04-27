package com.example.carecareforeldres.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Medecin implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMedecin;
    private Boolean disponible;
    private Integer user;
    String nom;
    String prenom;
    String mail;
    @Enumerated(EnumType.STRING)
    private Specialite specialite;
   // @OneToMany(cascade = CascadeType.ALL,mappedBy = "medecin",fetch = FetchType.EAGER)
    //List<Rdv> rdvs=new ArrayList<>();
    @ManyToOne(cascade = CascadeType.ALL)
    Etablissement etablissement;
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "medecin")
    List<Patient>patients;
}
