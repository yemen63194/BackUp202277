package com.example.carecareforeldres.Service;

import com.example.carecareforeldres.DTO.PlatWithIngredientsDTO;
import com.example.carecareforeldres.Entity.*;
import com.example.carecareforeldres.Repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PlatService implements IServicePlat {

    PlatRepository platRepository;
    IngredientRepository ingredientRepository;
    PatientRepository patientREpository;
    CuisinierRepository cuisinierRepository;
    RestaurantRepository restaurantRepository;
    private final JavaMailSender emailSender;
    UserRepository userRepository;

RepasRepository repasRepository;

    @Override
    public Plat addPlatDTO(PlatWithIngredientsDTO platDTO,Integer IDCuisinier) {
        Plat plat = new Plat();
        Cuisinier cuisinier =cuisinierRepository.findById(IDCuisinier).orElse(null);
        plat.setNomPlat(platDTO.getNomPlat());
        plat.setDescPlat(platDTO.getDescPlat());
        plat.setPrixPlat(platDTO.getPrixPlat());
        plat.setTypePlat(platDTO.getTypePlat());
        plat.setTypeRepas(platDTO.getTypeRepas());
        plat.setImage(platDTO.getImage());
        plat.setDatePlat(LocalDate.now());
        plat.setCuisinier(cuisinier);
        Integer  s =0;
        if (cuisinier != null) {
            s= cuisinier.getScore();
            s++;
            cuisinier.setScore(s);
            cuisinierRepository.save(cuisinier);

        }


        List<Ingredient> ingredients = new ArrayList<>();
        for (Integer ingredientId : platDTO.getIngredientIds()) {
            Ingredient ingredient = ingredientRepository.findById(ingredientId).orElse(null);
            if (ingredient != null) {
                ingredients.add(ingredient);
            }
        }
        plat.setIngredients((ingredients));
        return platRepository.save(plat);
    }

    public List<Plat> listPlatParResto(Integer idPatient){
        Patient patient=patientREpository.findById(idPatient).get();
        Restaurant resto=patient.getEtablissement().getRestaurant();
        return platRepository.listePlatRestaurantPar(resto.getIdRestaurant());

    }



    @Override
    public Plat addPlatPatient(Plat pt, Integer idPatient) {
        pt.setDatePlat(LocalDate.now());

        Patient patient=patientREpository.findById(idPatient).get();
        LocalDate l =LocalDate.now();
        boolean tst=testMaladie(pt,idPatient);
        if (tst){
        float nbCalories=0;
        float somme=0.0F;
        List<Ingredient> ingredients = pt.getIngredients();
         for (Ingredient ing :ingredients){
             somme+=ing.getCalorie();
         }

        // nbCalories= ingredientRepository.sumCalorieByPlatsRepasPatientIdAndPlatsDatePlat(idPatient, l  );
           // nbCalories += platRepository.getPlatByPatientId(idPatient).stream().map(plat->
             //       plat.getIngredients().stream().map(ingred->ingred.getCalorie()).reduce(0f,(a,b)->a+b)

            //).reduce(0f,(a,b)->a+b);
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
            log.info(String.valueOf(idPatient)+"////////////////////");
            //log.info(platRepository.getPlatByPatientId(idPatient).toString()+"////////////////////");

            log.info("nombre de calorie"+nbc+"//////nombre de calorie estimé:"+nbCalorieEstimee+"/////"+idPatient);
        if(somme <= (nbCalorieEstimee/3)) {
            if (nbc < nbCalorieEstimee) {
                pt.setDatePlat(LocalDate.now());
                return platRepository.save(pt);
            } else {
                log.info("tu posséde ton nbre de calories");
            }
        }else{
            log.info("Vous avez dépassé les calories allouéees pour votre repas");
        }
        }else {
            log.info("tu peux pas ajouter ce plat");
        }
        return pt ;

    }

    @Override
    public List<Plat> getAll() {
        return platRepository.findAll();
    }

    @Override
    public void remove(int idf) {
        Plat plat = platRepository.findById(idf).orElse(null);
        if (plat != null) {
            for (Repas repas : plat.getRepas()) {
                repas.getPlats().remove(plat);
                repasRepository.save(repas);
            }
        }

        // Supprimer le plat
        platRepository.deleteById(idf);;
    }

    @Override
    public Plat update(Plat res) {
        res.setDatePlat(res.getDatePlat());
//        sendEmail( "nasriamin300@gmail.com",  "nom");
        return platRepository.save(res);
    }







    @Override
    public Boolean testMaladie(Plat plat, Integer idPatient) {
        Patient patient = patientREpository.findById(idPatient).orElse(null);

        if (patient == null) {
            log.error("Patient not found with ID: " + idPatient);
            return false;
        }

        List<Maladie> maladiesPatient = patient.getMaladies();
        List<Ingredient> ingredients = plat.getIngredients();
        List<Maladie> maladiesIngredients = new ArrayList<>();

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


    @Scheduled(cron = "0 47 5 * * *")
    public void verifierPlats() {
        log.info("supri---------------------------");
        List<Plat> plats = platRepository.findAll();
        List<Plat> plats1 = new ArrayList<>();
        for (Plat plat : plats) {
            int sommeVotes = plat.getLikePlat() + plat.getDislikePlat();
float x=(plat.getDislikePlat() / (float)sommeVotes);
            log.info("supri---------------------------"+x);

            if (sommeVotes > 5 && (plat.getDislikePlat() / (float)sommeVotes) >= 0.7) {
                plats1.add(plat);
                Cuisinier cuisinier =plat.getCuisinier();
                int score=cuisinier.getScore()-3;
                cuisinier.setScore(score);
                cuisinierRepository.save(cuisinier);

            }
        }
        for (Plat plat : plats1) {
            log.info("Plat à supprimer - ID : " + plat.getIdPlat());
        }

        log.info("Nombre de plats à supprimer : " + plats1.size());

        platRepository.deleteAll(plats1);

    }


//    private void sendEmail(String email, String nom) throws MailException {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email);
//        message.setSubject("réprimander");
//        message.setText("Cher/Chère "+nom+",\n" +
//                "\n" +
//                "Je vous écris pour discuter d'une question importante concernant la qualité de nos plats, en particulier ceux préparés par vous-même. Récemment, nous avons reçu un retour d'information alarmant de la part de nos clients, indiquant que les plats que vous avez préparés ont reçu un taux de mécontentement exceptionnellement élevé.\n" +
//                "\n" +
//                "Après avoir analysé les données des avis clients, il est devenu évident que vos plats ont reçu un taux de désapprobation de près de 70%. Cela est inacceptable et met en évidence un problème sérieux qui doit être résolu de toute urgence.\n" +
//                "\n" +
//                "En tant que cuisinier au sein de notre établissement, vous avez la responsabilité de maintenir les normes les plus élevées en matière de qualité, de fraîcheur et de saveur de nos plats. Les retours négatifs de nos clients sont préoccupants car ils nuisent non seulement à notre réputation, mais aussi à notre capacité à fidéliser notre clientèle existante et à attirer de nouveaux clients.\n" +
//                "\n" +
//                "Je vous exhorte donc à examiner sérieusement vos méthodes de travail, à identifier les problèmes potentiels dans la préparation des plats et à prendre des mesures immédiates pour les corriger. Il est impératif que vous redoubliez d'efforts pour garantir que chaque plat que vous préparez rencontre les attentes de nos clients en termes de qualité, de goût et de présentation.\n" +
//                "\n" +
//                "De plus, je vous encourage à solliciter des feedbacks réguliers de la part des clients afin de mieux comprendre leurs préférences et leurs préoccupations. La communication ouverte et la rétroaction constructive sont essentielles pour améliorer continuellement nos services et notre offre culinaire.\n" +
//                "\n" +
//                "Sachez que nous sommes là pour vous soutenir dans votre parcours d'amélioration et que nous sommes disponibles pour vous fournir toute l'aide ou les ressources dont vous pourriez avoir besoin pour réussir dans votre rôle de cuisinier.\n" +
//                "\n" +
//                "Toutefois, veuillez prendre conscience que des mesures disciplinaires pourraient être prises si des améliorations significatives ne sont pas observées dans un avenir proche. Nous avons confiance en vos compétences et en votre engagement envers l'excellence, et nous sommes convaincus que vous prendrez les mesures nécessaires pour rectifier la situation.\n" +
//                "\n" +
//                "Je vous remercie de votre attention à cette question et de votre engagement continu envers la qualité de notre service. Si vous avez des questions ou des préoccupations, n'hésitez pas à me contacter pour en discuter davantage.\n" +
//                "\n" +
//                "Cordialement," );
//        emailSender.send(message);
//    }
}
