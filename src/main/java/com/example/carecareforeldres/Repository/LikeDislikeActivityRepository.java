package com.example.carecareforeldres.Repository;

import com.example.carecareforeldres.Entity.Activity;
import com.example.carecareforeldres.Entity.LikeDislikeActivity;
import com.example.carecareforeldres.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeDislikeActivityRepository extends JpaRepository<LikeDislikeActivity,Long> {
    LikeDislikeActivity findByActivityAndPatient(Activity activity, Patient patient);

}
