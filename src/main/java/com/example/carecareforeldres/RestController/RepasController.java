package com.example.carecareforeldres.RestController;

import com.example.carecareforeldres.DTO.RepasAvecPlatsDTO;
import com.example.carecareforeldres.Entity.Repas;
import com.example.carecareforeldres.Service.IServiceRepas;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/repas")
@CrossOrigin("*")
public class RepasController {
    IServiceRepas iServiceRepas;

    @PostMapping("/add")
    public Repas ajouterRepas(@RequestBody Repas res){
        Repas p1=iServiceRepas.addPlat(res);
        return p1;
    }

    @GetMapping("/retrive_all_repas")
    public List<Repas> retrieveRepasList(){

        return iServiceRepas.getAll();
    }



    @PutMapping("/update_repas")
    public Repas updateFood(@RequestBody Repas repas){

        return iServiceRepas.update(repas);
    }

    @DeleteMapping("/delete_repas/{repasId}")
    public void deleteRepas(@PathVariable("repasId") Integer repasId){
        iServiceRepas.remove(repasId);
    }

    @PostMapping("/affecter-repas/{idUser}")
    public Repas affecterRepasAUser(@RequestBody RepasAvecPlatsDTO repas, @PathVariable Integer idUser) {

            Repas repasAffecte = iServiceRepas.AffecterRepasAUser(repas, idUser);
            return repasAffecte;
    }
    @PostMapping("/addRepasWithPlat")
    public Repas addRepasWithPlat(@RequestBody RepasAvecPlatsDTO repasDTO) {
        return iServiceRepas.addRepasAvecPlats(repasDTO);
    }
}
