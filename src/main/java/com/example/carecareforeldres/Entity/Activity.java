package com.example.carecareforeldres.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.repository.cdi.Eager;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Activity")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Activity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActivity;

    private String nomActivity;
    private String image ;

    private LocalDate dateActivity;
    private int rating; // Le rating de l'activité en étoiles
    private String perfermance;
    private Integer likeActivity;
    private Integer dislikeActivity;
    @Enumerated(EnumType.STRING)
    private TypeActivity typeActivity;
    @Enumerated(EnumType.STRING)
    private EtatActivite etat = EtatActivite.EN_ATTENTE;
    private boolean notified = false;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Patient> Patienttts = new ArrayList<>();
    // @OneToMany(cascade = CascadeType.ALL, mappedBy="activity")
    //  private List<User> Users;

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    Organisateur organisateur;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "activity")
    @JsonIgnore
    List<LikeDislikeActivity> likeDislikeActivities = new ArrayList<>();


}