package com.example.carecareforeldres.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Patient implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idpatient;
    private Integer user;
    String nom;
    String prenom;
    String mail;
    @Enumerated(EnumType.STRING)
    private TypePatient typatient;
    private Boolean archiver;
    private Float poid;
    private Float longueur;
    private LocalDate datedeNais;
    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    @ManyToOne(cascade = CascadeType.ALL)
    Ambulance ambulance;
    @ManyToOne(cascade = CascadeType.ALL)
    Etablissement etablissement;
    @ManyToOne(cascade = CascadeType.ALL)
    Infermier infermier;
    @ManyToOne(cascade = CascadeType.ALL)
    Morgue morgue;
    @ManyToOne(cascade = CascadeType.ALL)
    Medecin medecin;
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "patient")
    @JsonIgnore
    List<Repas> repas;





    @ManyToMany(cascade = CascadeType.ALL,mappedBy = "patients")
    @JsonIgnore
    List<Maladie> maladies=new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "patient")
    List<LikeDislikePlat> likeDislikePlats;

@ManyToMany(cascade = CascadeType.ALL,mappedBy = "Patienttts")
    List<Activity> activity = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "patient")
    List<LikeDislikeActivity> likeDislikeActivities = new ArrayList<>();


    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "patient_activity_favori",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    private List<Activity> activityFavoris;
    public List<Activity> getActivityFavoris() {
        return activityFavoris;
    }

    public void setActivityFavoris(List<Activity> activityFavoris) {
        this.activityFavoris = activityFavoris;
    }
}
