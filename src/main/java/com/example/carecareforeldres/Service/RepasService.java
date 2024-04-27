package com.example.carecareforeldres.Service;

import com.example.carecareforeldres.DTO.RepasAvecPlatsDTO;
import com.example.carecareforeldres.Entity.*;
import com.example.carecareforeldres.Repository.PatientRepository;
import com.example.carecareforeldres.Repository.PlatRepository;
import com.example.carecareforeldres.Repository.RepasRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class RepasService implements IServiceRepas{

    RepasRepository repasRepository;
    PlatRepository platRepository;
    PatientRepository patientREpository;
    @Override
    public Repas addPlat(Repas pt) {

        return repasRepository.save(pt);
    }

    @Override
    public List<Repas> getAll() {
        return repasRepository.findAll();
    }

    @Override
    public void remove(int idf) {
        repasRepository.deleteById(idf);
    }

    @Override
    public Repas update(Repas res) {
        return repasRepository.save(res);
    }

    @Override
    public Repas addRepasAvecPlats(RepasAvecPlatsDTO repasDTO) {
        Repas repas = new Repas();
        repas.setDateRepas(LocalDate.now());
        repas.setTypeRepas(repasDTO.getTypeRepas());

        List<Plat> plats = new ArrayList<>();
        for (Integer platId : repasDTO.getPlatsIds()) {
            Plat plat = platRepository.findById(platId).orElse(null);
            if (plat != null) {
                plats.add(plat);
            }
        }
        repas.setPlats(plats);

        return repasRepository.save(repas);
    }



    @Transactional
    @Override
    public Repas AffecterRepasAUser(RepasAvecPlatsDTO repasDTO, Integer idPatient) {

        Repas repas = new Repas();
        repas.setDateRepas(LocalDate.now());
        repas.setTypeRepas(repasDTO.getTypeRepas());

        List<Plat> plats = new ArrayList<>();
        for (Integer platId : repasDTO.getPlatsIds()) {
            if (platId==null){
                continue;
            }
            Plat plat = platRepository.findById(platId).orElse(null);

            if (plat != null) {
                log.info(plat.toString()+ "platW**********");
                plats.add(plat);
            }
        }
        Patient pa=patientREpository.findById(idPatient).get();
        repas.setPatient(pa);
        repas.setPlats(plats);
        //repasRepository.save(repas);





        LocalDate dateActuelle = LocalDate.now();

        boolean tst = testMaladie(repas, idPatient);
        if (tst) {
        if (repas.getTypeRepas().equals(TypeRepas.PETIT_DEJEUNER)) {
            if (repasRepository.existsByPatientIdAndTypeRepasAndDate(idPatient, TypeRepas.PETIT_DEJEUNER, dateActuelle)) {
                log.info("Tu as déjà un repas de petit déjeuner pour aujourd'hui.");
            }else{
                Patient patient=patientREpository.findById(idPatient).get();

                List<Plat> pt = repas.getPlats();


                float nbCalories=0;
                float somme=0.0F;

                List<Ingredient> ingredients = new ArrayList<>();
                for(Plat plat:pt){
                    ingredients.addAll(plat.getIngredients());
                }
                for (Ingredient ing :ingredients){
                    somme+=ing.getCalorie();
                }

                // nbCalories= ingredientRepository.sumCalorieByPlatsRepasPatientIdAndPlatsDatePlat(idPatient, l  );

                nbCalories +=platRepository.calculateCaloriesConsumedByUserToday(idPatient);
                //  nbCalories += platRepository.getPlatByPatientId(idPatient).stream().map(plat->
                //        plat.getIngredients().stream().map(ingred->ingred.getCalorie()).reduce(0f,(a,b)->a+b) ).reduce(0f,(a,b)->a+b);
                log.info(String.valueOf(idPatient)+"////////////////////");
                // log.info(platRepository.getPlatByPatientId(idPatient).toString()+"////////////////////");
                log.info("nombre de calorie aujourd'hui"+nbCalories);
                float nbc=somme+nbCalories;

                //-----------------------------------Calcule nbCalorie estimé--------------------------------
                float longeur =patient.getLongueur();
                float poid = patient.getPoid();
                float nbCalorieEstimee=0.0F;
                LocalDate currentDate = LocalDate.now();
                int age = patientREpository.calculatePatientAgeById(idPatient);
                if(patient.getSexe().equals(Sexe.HOMME)){
                    ////
                    nbCalorieEstimee= (float) ((10*poid)+(6.25*longeur)-(5*age)+5);
                }

                if(patient.getSexe().equals(Sexe.FEMME)){
                    nbCalorieEstimee= (float) ((10*poid)+(6.25*longeur)-(5*age)-161);
                }
                log.info("nombre de calorie"+nbc+"//////nombre de calorie estimé:"+nbCalorieEstimee);
                if(somme <= (nbCalorieEstimee/3)) {
                    if (nbc < nbCalorieEstimee) {

                        log.info("///////////////////////////////////////////////////////////////////");

                        log.info("nombre de calorie aujourd'hui"+nbCalories);

                        return repasRepository.save(repas);

                    } else {
                        log.info("tu posséde ton nbre de calories");
                    }
                }else{
                    log.info("Vous avez dépassé les calories allouéees pour votre repas");
                }

        }
        }
        else if (repas.getTypeRepas().equals(TypeRepas.DEJEUNER)) {
            if (repasRepository.existsByPatientIdAndTypeRepasAndDate(idPatient, TypeRepas.DEJEUNER, dateActuelle)) {
                log.info("Tu as déjà un repas de  déjeuner pour aujourd'hui.");
            }else{
                Patient patient=patientREpository.findById(idPatient).get();

                List<Plat> pt = repas.getPlats();


                float nbCalories=0;
                float somme=0.0F;

                List<Ingredient> ingredients = new ArrayList<>();
                for(Plat plat:pt){
                    ingredients.addAll(plat.getIngredients());
                }
                for (Ingredient ing :ingredients){
                    somme+=ing.getCalorie();
                }

                // nbCalories= ingredientRepository.sumCalorieByPlatsRepasPatientIdAndPlatsDatePlat(idPatient, l  );

                nbCalories +=platRepository.calculateCaloriesConsumedByUserToday(idPatient);
                //  nbCalories += platRepository.getPlatByPatientId(idPatient).stream().map(plat->
                //        plat.getIngredients().stream().map(ingred->ingred.getCalorie()).reduce(0f,(a,b)->a+b) ).reduce(0f,(a,b)->a+b);
                log.info(String.valueOf(idPatient)+"////////////////////");
                // log.info(platRepository.getPlatByPatientId(idPatient).toString()+"////////////////////");
                log.info("nombre de calorie aujourd'hui"+nbCalories);
                float nbc=somme+nbCalories;

                //-----------------------------------Calcule nbCalorie estimé--------------------------------
                float longeur =patient.getLongueur();
                float poid = patient.getPoid();
                float nbCalorieEstimee=0.0F;
                LocalDate currentDate = LocalDate.now();
                int age = patientREpository.calculatePatientAgeById(idPatient);
                if(patient.getSexe().equals(Sexe.HOMME)){
                    ////
                    nbCalorieEstimee= (float) ((10*poid)+(6.25*longeur)-(5*age)+5);
                }

                if(patient.getSexe().equals(Sexe.FEMME)){
                    nbCalorieEstimee= (float) ((10*poid)+(6.25*longeur)-(5*age)-161);
                }
                log.info("nombre de calorie"+nbc+"//////nombre de calorie estimé:"+nbCalorieEstimee);
                if(somme <= (nbCalorieEstimee/3)) {
                    if (nbc < nbCalorieEstimee) {

                        log.info("///////////////////////////////////////////////////////////////////");

                        log.info("nombre de calorie aujourd'hui"+nbCalories);

                        return repasRepository.save(repas);

                    } else {
                        log.info("tu posséde ton nbre de calories");
                    }
                }else{
                    log.info("Vous avez dépassé les calories allouéees pour votre repas");
                }

        }
        }
        else if (repas.getTypeRepas().equals(TypeRepas.DINER)) {
            if (repasRepository.existsByPatientIdAndTypeRepasAndDate(idPatient, TypeRepas.DINER, dateActuelle)) {
                log.info("Tu as déjà un repas de diner pour aujourd'hui.");
            }else{
                Patient patient=patientREpository.findById(idPatient).get();

                List<Plat> pt = repas.getPlats();


                float nbCalories=0;
                float somme=0.0F;

                List<Ingredient> ingredients = new ArrayList<>();
                for(Plat plat:pt){
                    ingredients.addAll(plat.getIngredients());
                }
                for (Ingredient ing :ingredients){
                    somme+=ing.getCalorie();
                }

                // nbCalories= ingredientRepository.sumCalorieByPlatsRepasPatientIdAndPlatsDatePlat(idPatient, l  );

                nbCalories +=platRepository.calculateCaloriesConsumedByUserToday(idPatient);
              //  nbCalories += platRepository.getPlatByPatientId(idPatient).stream().map(plat->
                //        plat.getIngredients().stream().map(ingred->ingred.getCalorie()).reduce(0f,(a,b)->a+b) ).reduce(0f,(a,b)->a+b);
                log.info(String.valueOf(idPatient)+"////////////////////");
               // log.info(platRepository.getPlatByPatientId(idPatient).toString()+"////////////////////");
                log.info("nombre de calorie aujourd'hui"+nbCalories);
                float nbc=somme+nbCalories;

                //-----------------------------------Calcule nbCalorie estimé--------------------------------
                float longeur =patient.getLongueur();
                float poid = patient.getPoid();
                float nbCalorieEstimee=0.0F;
                LocalDate currentDate = LocalDate.now();
                int age = patientREpository.calculatePatientAgeById(idPatient);
                if(patient.getSexe().equals(Sexe.HOMME)){
                    ////
                    nbCalorieEstimee= (float) ((10*poid)+(6.25*longeur)-(5*age)+5);
                }

                if(patient.getSexe().equals(Sexe.FEMME)){
                    nbCalorieEstimee= (float) ((10*poid)+(6.25*longeur)-(5*age)-161);
                }
                log.info("nombre de calorie"+nbc+"//////nombre de calorie estimé:"+nbCalorieEstimee);
                if(somme <= (nbCalorieEstimee/3)) {
                    if (nbc < nbCalorieEstimee) {

                        log.info("///////////////////////////////////////////////////////////////////");

                        log.info("nombre de calorie aujourd'hui"+nbCalories);

                        return repasRepository.save(repas);

                    } else {
                        log.info("tu posséde ton nbre de calories");
                    }
                }else{
                    log.info("Vous avez dépassé les calories allouéees pour votre repas");
                }

        }
        }
    }else {
            log.info("Le patient et les ingrédients ne comportent pas la même maladie : ");
        }

        return repas;

    }

    public Boolean testMaladie(Repas repas, Integer idPatient) {
        Patient patient = patientREpository.findById(idPatient).orElse(null);

            if (patient == null) {
                log.error("Patient not found with ID: " + idPatient);
                return false;
            }

            List<Maladie> maladiesPatient = patient.getMaladies();
            List<Plat> plat = repas.getPlats();
            List<Maladie> maladiesIngredients = new ArrayList<>();
            List<Ingredient> ingredients = new ArrayList<>();
            for (Plat plat1 : plat) {
                ingredients.addAll(plat1.getIngredients());
            }

            for (Ingredient ingredient : ingredients) {
                maladiesIngredients.addAll(ingredient.getMaladies());
            }

            for (Maladie maladiePatient : maladiesPatient) {
                boolean maladieFound = false;

                for (Maladie maladieIngredient : maladiesIngredients) {
                    if (maladieIngredient.getNom().equals(maladiePatient.getNom())) {
                        maladieFound = true;
                        break;
                    }
                }

                if (!maladieFound) {
                    log.info("Le patient et les ingrédients ne comportent pas la même maladie : " + maladiePatient.getNom());
                    return false;
                }
            }

            return true;
        }


}
