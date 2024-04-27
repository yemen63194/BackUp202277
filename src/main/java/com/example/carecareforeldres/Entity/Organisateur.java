package com.example.carecareforeldres.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Organisateur implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOrganisateur;
    private Integer user;
    @Enumerated(EnumType.STRING)
    private TypeBadge typeBadge;    @OneToMany(cascade = CascadeType.ALL, mappedBy="organisateur")
    @JsonIgnore
    private List<Activity> Activitys;
}
