package com.example.carecareforeldres.Repository;

import com.example.carecareforeldres.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeDislikeRepository extends JpaRepository<LikeDislikePlat,Integer> {

    LikeDislikePlat findByPatientAndPlat(Patient patient, Plat plat);
}
