package com.example.carecareforeldres.Service;

import com.example.carecareforeldres.Entity.*;
import com.example.carecareforeldres.Repository.CoungeRepository;
import com.example.carecareforeldres.Repository.CuisinierRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CoungeService implements IServiceCounge{
    CoungeRepository coungeRepository ;
    CuisinierRepository cuisinierRepository;


    @Override
    public Counge add(Counge res) {return coungeRepository.save(res);}
    @Override
    public List<Counge> getAll(){return coungeRepository.findAll();}


    @Override
    public void remove(int idf) {
        coungeRepository.deleteById(idf);}
    public Counge updateCoungeEtat(Integer id, EtatCounger newEtat) {
        Counge counge = coungeRepository.findById(id).get();
        counge.setEtatCounger(newEtat);
        return coungeRepository.save(counge);
    }




    @Override
    public Counge update(Counge counge, Integer CuisinierId) {
        Cuisinier cuisinier=cuisinierRepository.findById(CuisinierId).get();
        counge.setEtatCounger(EtatCounger.EN_COUR);
        counge.setCuisinierC(cuisinier);
        counge.setDateAjout(LocalDate.now());
        List<Counge> congesDuCuisinier = cuisinier.getCounges();

        boolean aPrisConge = aPrisCongeDerniersQuatreMois(congesDuCuisinier);


        LocalDate dateDebut = counge.getDateDebut(); // Convertir en LocalDate
        LocalDate dateFin = counge.getDateFin(); // Convertir en LocalDate
        LocalDate currentDate = LocalDate.now();
        int joursCongesCetteAnnee = coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate);
        int dureeNouvelleConge = (int) ChronoUnit.DAYS.between(dateDebut, dateFin); // Calculer la durée du congé directement avec les LocalDate

        long differenceEnJours = ChronoUnit.DAYS.between(currentDate, dateDebut);

        if (counge.getDateFin().isBefore(counge.getDateDebut()) ) {

            if (differenceEnJours < 3) {
                log.info("La date de début du congé doit être au moins 3 jours après la date actuelle.");
                return null;
            }
            log.info("La date de fin est antérieure à la date de début.");
            return null;
        }
        else {
            if (!aPrisConge) {

                if(counge.getTypeCounger().equals(TypeCounger.COUNGER_MATERNITE) && cuisinier.getSexe().equals(Sexe.FEMME) && dureeNouvelleConge < 45 && coungeRepository.countCongesByCuisinierAndYear(cuisinier,currentDate,TypeCounger.COUNGER_MATERNITE) == 0){
                    return coungeRepository.save(counge);
                }
                if(counge.getTypeCounger().equals(TypeCounger.COUGER_PARENTALE) && cuisinier.getSexe().equals(Sexe.HOMME) && dureeNouvelleConge < 3 && coungeRepository.countCongesByCuisinierAndYear(cuisinier,currentDate,TypeCounger.COUGER_PARENTALE) == 0){
                    return coungeRepository.save(counge);
                }
                log.info("tu a déjà pris un congé récemment");
                return counge;
            }


            if (joursCongesCetteAnnee + dureeNouvelleConge > 30) {
                if(counge.getTypeCounger().equals(TypeCounger.COUNGER_MATERNITE)  && coungeRepository.countCongesByCuisinierAndYear(cuisinier,currentDate,TypeCounger.COUNGER_MATERNITE) == 0){
                    if (cuisinier.getSexe().equals(Sexe.FEMME) && dureeNouvelleConge < 45) {
                        return coungeRepository.save(counge);
                    }
                }
                if(counge.getTypeCounger().equals(TypeCounger.COUGER_PARENTALE) && cuisinier.getSexe().equals(Sexe.HOMME) && dureeNouvelleConge < 3 && coungeRepository.countCongesByCuisinierAndYear(cuisinier,currentDate,TypeCounger.COUGER_PARENTALE) == 0){
                    return coungeRepository.save(counge);
                }
                log.info("Le cuisinier aura plus de 30 jours de congé cette année après l'ajout de cette nouvelle demande de congé.");
                return null;
            }
            return coungeRepository.save(counge);
        }
    }

    @Scheduled(cron = "0 0 11 ? * TUE")
    public void supprimerCongesRefuses() {
        List<Counge> congesRefuses = coungeRepository.findByEtatCounger(EtatCounger.REFUSER);
        coungeRepository.deleteAll(congesRefuses);
        for (Counge v:congesRefuses) {
            log.info("conger de l'id  "+v.getId()+ "   supprimer");
        }
    }

    @Override
    public ResponseEntity<?> DemandeCoungeCuisine(Counge counge, Integer CuisinierId) {
        try {
            Cuisinier cuisinier = cuisinierRepository.findById(CuisinierId).orElseThrow(() -> new NotFoundException("Cuisinier non trouvé"));
            counge.setEtatCounger(EtatCounger.EN_COUR);
            counge.setCuisinierC(cuisinier);
            counge.setDateAjout(LocalDate.now());
            List<Counge> congesDuCuisinier = cuisinier.getCounges();

            boolean aPrisConge;
            try {
                aPrisConge = aPrisCongeDerniersQuatreMois(congesDuCuisinier);
            } catch (Exception e) {
                throw new ErrorResponseException("Erreur lors de la vérification des congés du cuisinier", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            LocalDate dateDebut;
            LocalDate dateFin;
            LocalDate currentDate = LocalDate.now();
            try {
                dateDebut = counge.getDateDebut();
                dateFin = counge.getDateFin();
                if (dateDebut == null || dateFin == null) {
                    throw new ErrorResponseException("La date de début ou la date de fin du congé est null.", HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                throw new ErrorResponseException("Erreur lors de la récupération des dates de congé", HttpStatus.BAD_REQUEST);
            }

            int joursCongesCetteAnnee;
            int dureeNouvelleConge;
            try {
                joursCongesCetteAnnee = coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate);
                dureeNouvelleConge = (int) ChronoUnit.DAYS.between(dateDebut, dateFin);
            } catch (Exception e) {
                throw new ErrorResponseException("Erreur lors du calcul de la durée des congés", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            long differenceEnJours = ChronoUnit.DAYS.between(currentDate, dateDebut);

            if (counge.getDateFin().isBefore(counge.getDateDebut())) {
                if (differenceEnJours < 3) {
                    throw new ErrorResponseException("La date de début du congé doit être au moins 3 jours après la date actuelle.", HttpStatus.BAD_REQUEST);
                }
                throw new ErrorResponseException("La date de fin est antérieure à la date de début.", HttpStatus.BAD_REQUEST);
            } else {
                if (!aPrisConge) {
                    if (counge.getTypeCounger().equals(TypeCounger.COUNGER_MATERNITE) && cuisinier.getSexe().equals(Sexe.FEMME) && dureeNouvelleConge < 45 && coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate, TypeCounger.COUNGER_MATERNITE) == 0) {
                        coungeRepository.save(counge);
                        return new ResponseEntity<>("Coungé Ajouter avec succès", HttpStatus.OK);
                    }
                    if (counge.getTypeCounger().equals(TypeCounger.COUGER_PARENTALE) && cuisinier.getSexe().equals(Sexe.HOMME) && dureeNouvelleConge < 3 && coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate, TypeCounger.COUGER_PARENTALE) == 0) {
                        coungeRepository.save(counge);
                        return new ResponseEntity<>("Coungé Ajouter avec succès", HttpStatus.OK);
                    }
                    throw new ErrorResponseException("tu a déjà pris un congé récemment", HttpStatus.BAD_REQUEST);
                }

                if (joursCongesCetteAnnee + dureeNouvelleConge > 30) {
                    if (counge.getTypeCounger().equals(TypeCounger.COUNGER_MATERNITE) && coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate, TypeCounger.COUNGER_MATERNITE) == 0) {
                        if (cuisinier.getSexe().equals(Sexe.FEMME) && dureeNouvelleConge < 45) {
                            coungeRepository.save(counge);
                            return new ResponseEntity<>("Coungé Ajouter avec succès", HttpStatus.OK);
                        } else {
                            throw new ErrorResponseException("Le congé de maternité est limité à 45 jours pour les femmes", HttpStatus.BAD_REQUEST);
                        }
                    }
                    if (counge.getTypeCounger().equals(TypeCounger.COUGER_PARENTALE) && cuisinier.getSexe().equals(Sexe.HOMME) && dureeNouvelleConge < 3 && coungeRepository.countCongesByCuisinierAndYear(cuisinier, currentDate, TypeCounger.COUGER_PARENTALE) == 0) {
                        coungeRepository.save(counge);
                        return new ResponseEntity<>("Coungé Ajouter avec succès", HttpStatus.OK);
                    }
                    throw new ErrorResponseException("Le cuisinier aura plus de 30 jours de congé cette année après l'ajout de cette nouvelle demande de congé.", HttpStatus.BAD_REQUEST);
                }

                coungeRepository.save(counge);
                return new ResponseEntity<>("Coungé Ajouter avec succès", HttpStatus.OK);
            }
        } catch (ErrorResponseException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            throw new ErrorResponseException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public boolean aPrisCongeDerniersQuatreMois(List<Counge> congés) {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime ilYaQuatreMois = maintenant.minusMonths(2);

        for (Counge congé : congés) {
            LocalDate dateDebutConge = congé.getDateDebut();
            LocalDateTime dateDebutLocalDateTime = dateDebutConge.atStartOfDay(); // Convertir LocalDate en LocalDateTime en ajoutant une heure arbitraire

            if (dateDebutLocalDateTime.isAfter(ilYaQuatreMois) && dateDebutLocalDateTime.isBefore(maintenant)) {
                return true;
            }
        }

        return false;
    }

}