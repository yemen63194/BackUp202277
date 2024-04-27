package com.example.carecareforeldres.Service;

import com.example.carecareforeldres.Entity.Activity;
import com.example.carecareforeldres.Entity.EtatActivite;
import com.example.carecareforeldres.Entity.LikeDisliketRate;
import com.example.carecareforeldres.Entity.TypeActivity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IActivityService {
    List<Activity> retrieveAllActivity();

    Activity addActivity(Activity a);

    Activity updateActivity(Activity a);

    Activity retrieveActivity(Long idActivity);

    void removeActivity(Long idActivity);


    ResponseEntity<?> registerPatientToActivity(Long idActivity, int idPatient);

    ResponseEntity<?> registerOrganisateurToActivity(Activity activity, int idOrganisateur);
    public Map<LocalDate, Map<TypeActivity, Long>> getQualityTrend() ;

    public void reactToActivity(Long activityId, int patientId, LikeDisliketRate reactionType) ;
    public Map<String, Object> assignRatingsAndBadges() ;

    List<Activity> getTopThreeActivitiesByBadge();

     int numberOflikesofActivities(Long idActivity);

     int numberOfDisikesofActivity(Long idActivity);

    public void accepterActivite(Long idActivite) ;

    public void refuserActivite(Long idActivite) ;

    public List<Activity> getActivitiesByEtat(EtatActivite etat) ;
    public void addActivityToFavoris(Integer idPatient, Long idActivity) ;
    public void removeActivityFromFavoris(Integer idPatient, Long idActivity) ;
    public List<Activity> getActivtyFavorisByPatientId(Integer idPatient) ;

     List<Activity> getActivitiesForTomorrow() ;

     void sendActivityNotifications() throws Exception ;


    }
