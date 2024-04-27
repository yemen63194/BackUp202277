package com.example.carecareforeldres.Service;

import com.example.carecareforeldres.Entity.*;
import com.example.carecareforeldres.Repository.*;
import com.example.carecareforeldres.demo.EmailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityService implements  IActivityService {
    ActivityRepository activityRepository;
    PatientRepository patientRepository;

    UserRepository userRepository;

    EmailService emailService;

    LikeDislikeActivityRepository likeDislikeActivityRepository;

    OrganisateurRepository organisateurRepository;
    @Override
    public List<Activity> retrieveAllActivity() {
        return activityRepository.findAll();
    }

    @Override
    public Activity addActivity(Activity s) {
        return activityRepository.save(s);
    }

    @Override
    public Activity updateActivity(Activity s) {
        return activityRepository.save(s);
    }

    @Override
    public Activity retrieveActivity(Long idActivity) {
        return activityRepository.findById(idActivity).get();
    }

    @Override
    public void removeActivity(Long idActivity) {
        activityRepository.deleteById(idActivity);
    }

    @Override
    public ResponseEntity<?> registerPatientToActivity(Long idActivity, int idPatient) {
        Activity activity = activityRepository.findById(idActivity).get();
        Patient patient = patientRepository.findById(idPatient).get();
        int idH = patient.getUser();
        User user = userRepository.findById(idH).orElse(null);

        if(activity.getPatienttts().contains(patient)){
            throw new RuntimeException("Vous êtes déjà inscrit à cette activité");
        }
        activity.getPatienttts().add(patient);
        activityRepository.save(activity);

        String userEmail = user.getEmail();
        log.info("////////////////"+userEmail);
        String subject = "Confirmation d'inscription a l'activité";
        String message = "Vous avez été inscrit avec succès a activite. Merci!";
        try {
            emailService.sendEmail(userEmail, subject, message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'e-mail de confirmation : " + e.getMessage());
            // Gérer l'erreur d'envoi d'e-mail
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'envoi de l'e-mail de confirmation.");
        }
        return ResponseEntity.ok().body("{\"message\": \"Votre demande a été bien prise en compte, un Email de confirmation vous a été envoyé.\"}");
    }





    @Override
    public ResponseEntity<?> registerOrganisateurToActivity(Activity activity, int idOrganisateur) {
        Organisateur organisateur = organisateurRepository.findById(idOrganisateur).get();

        int idH = organisateur.getUser();
        User user = userRepository.findById(idH).orElse(null);
        activity.setOrganisateur(organisateur);
        activityRepository.save(activity);
        String userEmail = user.getEmail();
        log.info("////////////////"+userEmail);
        String subject = "Confirmation d'inscription a l'activité";
        String message = "Vous avez été inscrit avec succès a activite. Merci!";
        try {
            emailService.sendEmail(userEmail, subject, message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'e-mail de confirmation : " + e.getMessage());
            // Gérer l'erreur d'envoi d'e-mail
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'envoi de l'e-mail de confirmation.");
        }
        return ResponseEntity.ok().body("{\"message\": \"Votre demande d'organisation a été bien prise en compte, un Email de confirmation vous a été envoyé.\"}");

    }




    // @Override
    // public Activity registerPatientToActivity(Long idActivity, int idPatient) {
    //  Activity activity = activityRepository.findById(idActivity).get();
    //  Patient patient = patientRepository.findById(idPatient).get();

    //  activity.setPatienttt(patient);
    // activityRepository.save(activity);

    //   return activity;
    // }




    @Override
    public Map<LocalDate, Map<TypeActivity, Long>> getQualityTrend() {
        LocalDate startDate = LocalDate.now().minusDays(300000); // Par exemple, obtenir la tendance pour les 30 derniers jours
        LocalDate endDate = LocalDate.now();

        Map<LocalDate, Map<TypeActivity, Long>> qualityTrend = new HashMap<>();
        List<Object[]> qualityData = activityRepository.findQualityTrendByDateRange(startDate, endDate);

        for (Object[] data : qualityData) {
            LocalDate entryDate = (LocalDate) data[0];
            TypeActivity typeActivity = (TypeActivity) data[1];
            Long activityCount = (Long) data[2]; // Nombre d'occurrences de cette activité pour cette date

            if (!qualityTrend.containsKey(entryDate)) {
                qualityTrend.put(entryDate, new HashMap<>());
            }

            Map<TypeActivity, Long> qualityCounts = qualityTrend.get(entryDate);
            qualityCounts.put(typeActivity, activityCount);
        }

        return qualityTrend;
    }

    @Override
    public void reactToActivity(Long activityId, int patientId, LikeDisliketRate reactionType) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        Patient patient = patientRepository.findById(patientId).orElse(null);

        if (activity != null && patient != null) {
            if (activity.getLikeActivity() == null) {
                activity.setLikeActivity(0); // Initialisez à zéro si null
            }
            if (activity.getDislikeActivity() == null) {
                activity.setDislikeActivity(0); // Initialisez à zéro si null
            }
            LikeDislikeActivity existingReaction = likeDislikeActivityRepository.findByActivityAndPatient(activity, patient);

            if (existingReaction != null) {
                if (existingReaction.getReactionType() == reactionType) {
                    return;
                } else {
                    // Si le patient change sa réaction, mettre à jour le compteur
                    if (reactionType == LikeDisliketRate.LIKE) {
                        activity.setLikeActivity(activity.getLikeActivity() + 1);
                        activity.setDislikeActivity(activity.getDislikeActivity() - 1);
                    } else {
                        activity.setLikeActivity(activity.getLikeActivity() - 1);
                        activity.setDislikeActivity(activity.getDislikeActivity() + 1);
                    }
                    existingReaction.setReactionType(reactionType);
                    likeDislikeActivityRepository.save(existingReaction);
                }
            } else {
                LikeDislikeActivity newReaction = new LikeDislikeActivity();
                newReaction.setActivity(activity);
                newReaction.setPatient(patient);
                newReaction.setReactionType(reactionType);
                likeDislikeActivityRepository.save(newReaction);

                if (reactionType == LikeDisliketRate.LIKE) {
                    activity.setLikeActivity(activity.getLikeActivity() + 1);
                } else {
                    activity.setDislikeActivity(activity.getDislikeActivity() + 1);
                }
            }
            activityRepository.save(activity);
            patientRepository.save(patient);

        }
    }

    @Override
    public List<Activity> getTopThreeActivitiesByBadge() {
        return null;
    }

    @Override
    public Map<String, Object> assignRatingsAndBadges() {
        List<Activity> activities = activityRepository.findAll();
        Map<String, Object> results = new HashMap<>(); // Map to store activity and organizer details

        for (Activity activity : activities) {
            int totalLikes = activity.getLikeActivity() != null ? activity.getLikeActivity() : 0;
            int totalDislikes = activity.getDislikeActivity() != null ? activity.getDislikeActivity() : 0;

            int rating = Math.round((float) totalLikes / (totalLikes + totalDislikes) * 5); // Calculate rating between 0-5 based on likes and dislikes

            String badge;
            if (rating >= 4) {
                badge = "Excellent";
                activity.setPerfermance(badge); // Set badge on the activity for reference (optional)
            } else if (rating >= 3) {
                badge = "Bon";
                activity.setPerfermance(badge); // Set badge on the activity for reference (optional)
            } else if (rating >= 2) {
                badge = "Moyen";
                activity.setPerfermance(badge); // Set badge on the activity for reference (optional)
            } else {
                badge = "A Revoir";
                activity.setPerfermance(badge); // Set badge on the activity for reference (optional)
            }

            activity.setRating(rating);

            // Update organizer badge based on rating
            Organisateur organizer = activity.getOrganisateur();


                // Persist organizer changes (assuming organizer is not already managed)
            if (organizer != null) {
                TypeBadge organizerBadge;
                if (rating >= 4) {
                    organizerBadge = TypeBadge.GOLD;
                } else if (rating >= 3) {
                    organizerBadge = TypeBadge.SILVER;
                } else {
                    organizerBadge = TypeBadge.BRONZE;
                }
                organizer.setTypeBadge(organizerBadge);
                organisateurRepository.save(organizer);

                try {
                    int id = activity.getOrganisateur().getUser();
                    ; // Assuming 'nom' is the attribute for organizer's name
                    User user = userRepository.findById(id).get();
                    if (user != null) {
                        String   organizerFirstName = user.getFirstname();
                        String organizerLastName = user.getLastname();
                        Map<String, Object> activityData = new HashMap<>();
                        activityData.put("activityName", activity.getNomActivity()); // Add activity name
                        activityData.put("activityRating", rating); // Add activity rating
                        activityData.put("organizerFirstName", organizerFirstName); // Add organizer name
                        activityData.put("organizerLastName", organizerLastName);
                        activityData.put("organizerBadge", organizerBadge); // Add organizer badge

                        results.put(activity.getNomActivity(), activityData); // Use activity name as key
                        activityRepository.save(activity);
                    }
                } catch (NullPointerException e) {
                    // Handle NullPointerException gracefully (e.g., log the issue)
                    System.err.println("NullPointerException while retrieving organizer name for activity: " + activity.getNomActivity());
                }

            }
        }
        return results;

    }



  // @Override
   // public List<Activity> getTopThreeActivitiesByBadge() {
       // Map<Activity, Integer> ratingsAndBadges = assignRatingsAndBadges(); // Ensure ratings and badges are assigned

        // Sort activities by rating (descending) and badge (ascending)
     //   List<Activity> sortedActivities = new ArrayList<>(ratingsAndBadges.keySet());
     //   sortedActivities.sort((a1, a2) -> {
      //      int ratingComparison = Integer.compare(ratingsAndBadges.get(a2), ratingsAndBadges.get(a1));
      //      if (ratingComparison == 0) {
       //         return a1.getBadge().compareTo(a2.getBadge());
       //     }
      //      return ratingComparison;
      //  });

      //  return sortedActivities.subList(0, Math.min(sortedActivities.size(), 3)); // Return top 3 activities
   // }

    @Override
    public int numberOfDisikesofActivity(Long idActivity){
        try {
            Activity activity = activityRepository.findById(idActivity).orElse(null);
            if (activity == null) {
                return 0;
            }
            int dislikes = 0;
            for (LikeDislikeActivity e : activity.getLikeDislikeActivities()) {
                if (e.getReactionType().equals(LikeDisliketRate.DISLIKE)) {
                    dislikes++;
                }
            }
            return dislikes;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    @Override
    public int numberOflikesofActivities(Long idActivity){
        try {
            Activity activity = activityRepository.findById(idActivity).orElse(null);
            if (activity == null) {
                return 0;
            }
            int likes = 0;
            for (LikeDislikeActivity e : activity.getLikeDislikeActivities()) {
                if (e.getReactionType().equals(LikeDisliketRate.LIKE)) {
                    likes++;
                }
            }
            return likes;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void accepterActivite(Long idActivite) {
        Activity activity = activityRepository.findById(idActivite).orElse(null);
        if (activity != null) {
            activity.setEtat(EtatActivite.ACCEPTE);
            activityRepository.save(activity);
            int idH = activity.getOrganisateur().getUser();

            // Envoyer un e-mail à l'organisateur
            User user = userRepository.findById(idH).orElse(null);
            String userEmail = user.getEmail();
            String subject = "Votre activité a été acceptée";
            String message = "Votre activité " + activity.getNomActivity() + " a été acceptée avec succès.";
            try {
                emailService.sendEmail(userEmail, subject, message);
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de l'e-mail à l'organisateur : " + e.getMessage());
                // Gérer l'erreur d'envoi d'e-mail
            }
        }
    }


    @Override
    public void refuserActivite(Long idActivite) {
        Activity activity = activityRepository.findById(idActivite).orElse(null);
        if (activity != null) {
            activity.setEtat(EtatActivite.REFUSE);
            activityRepository.save(activity);
            int idH = activity.getOrganisateur().getUser();

            // Envoyer un e-mail à l'organisateur
            User user = userRepository.findById(idH).orElse(null);
            String userEmail = user.getEmail();
            String subject = "Votre activité a été refusée";
            String message = "Votre activité " + activity.getNomActivity() + " a été refusée.";
            try {
                emailService.sendEmail(userEmail, subject, message);
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de l'e-mail à l'organisateur : " + e.getMessage());
                // Gérer l'erreur d'envoi d'e-mail
            }
        }
    }


    @Override
    public List<Activity> getActivitiesByEtat(EtatActivite etat) {
        return activityRepository.findByEtat(etat);
    }
    @Override
    public void addActivityToFavoris(Integer idPatient, Long idActivity) {
        Optional<Patient> optionalUtilisateur = patientRepository.findById(idPatient);
        if (optionalUtilisateur.isPresent()) {
            Patient patient = optionalUtilisateur.get();
            Activity activity = retrieveActivity(idActivity); // Assurez-vous que cette méthode est correctement implémentée

            // Vérifier si le produit est déjà dans les favoris de l'utilisateur
            if (patient.getActivityFavoris().contains(activity)) {
                throw new IllegalArgumentException("Le produit est déjà dans les favoris de l'utilisateur.");
            }

            patient.getActivityFavoris().add(activity);
            patientRepository.save(patient);
            log.info("Le produit avec l'ID {} a été ajouté aux favoris de l'utilisateur avec l'ID {}.", idActivity, idPatient);
        } else {
            throw new EntityNotFoundException("Aucun utilisateur trouvé avec l'identifiant : " + idPatient);
        }
    }

    @Override
    public void removeActivityFromFavoris(Integer idPatient, Long idActivity) {
        Optional<Patient> optionalUtilisateur = patientRepository.findById(idPatient);
        if (optionalUtilisateur.isPresent()) {
            Patient patient = optionalUtilisateur.get();
            Activity activity = retrieveActivity(idActivity); // Assurez-vous que cette méthode est correctement implémentée

            patient.getActivityFavoris().remove(activity);
            patientRepository.save(patient);
            log.info("Le produit avec l'ID {} a été retiré des favoris de l'utilisateur avec l'ID {}.", idActivity, idPatient);
        } else {
            throw new EntityNotFoundException("Aucun utilisateur trouvé avec l'identifiant : " + idPatient);
        }
    }




    @Override
    public List<Activity> getActivtyFavorisByPatientId(Integer idPatient) {
        Optional<Patient> optionalUtilisateur = patientRepository.findById(idPatient);
        if (optionalUtilisateur.isPresent()) {
            Patient patient = optionalUtilisateur.get();
            return patient.getActivityFavoris();
        } else {
            throw new EntityNotFoundException("Aucun patient trouvé avec l'identifiant : " + idPatient);
        }
    }

    @Override
    public List<Activity> getActivitiesForTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return activityRepository.findEventsForTomorrow(tomorrow);
    }

    @Override
    public void sendActivityNotifications() throws Exception {

    }

  //  @Scheduled(fixedRate = 3000) // Run every day (24 * 60 * 60 * 1000 milliseconds)
    //@Scheduled(cron = "0 0 9 * * *")
    public void sendEventNotifications() throws Exception {
        System.err.println(LocalDate.now());
        List<Activity> activities = getActivitiesForTomorrow(); // Implement this method to get events happening tomorrow
        for (Activity activity : activities) {
            for(Patient patient : activity.getPatienttts()){
                if (!activity.isNotified()) {
                    System.err.println(activity.getIdActivity());
                    int idH = patient.getUser();

                    // Envoyer un e-mail à l'organisateur
                    User user = userRepository.findById(idH).orElse(null);
                    String userEmail = user.getEmail();
                    String subject = "Reminder: " + activity.getNomActivity() + " Tomorrow!";
                    String body = "Don't forget, the activity " + activity.getNomActivity() + " is happening tomorrow at " + activity.getDateActivity();
                    try {
                        emailService.sendEmail(userEmail, subject, body);
                        log.info("E-mail sent successfully to patient: " + userEmail);
                        log.info("Notification sent successfully for activity: " + activity.getIdActivity());
                    } catch (Exception e) {
                        log.error("Erreur lors de l'envoi de l'e-mail à l'organisateur : " + e.getMessage());
                        // Gérer l'erreur d'envoi d'e-mail
                    }


                   activity.setNotified(true);
                    activityRepository.save(activity);

                }
            }
        }
    }
}
