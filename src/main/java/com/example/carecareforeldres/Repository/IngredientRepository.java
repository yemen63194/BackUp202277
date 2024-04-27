package com.example.carecareforeldres.Repository;

import com.example.carecareforeldres.Entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient,Integer> {
    //float sumCalorieByPlatsRepasPatientIdAndPlatsDatePlat(Integer patientId, LocalDate datePlat);
    @Query("SELECT i FROM Ingredient i WHERE i.consommable= true")
    List<Ingredient> ingredientConsommable();

    Ingredient findByNomIngredient(String nomIngredient);
}
