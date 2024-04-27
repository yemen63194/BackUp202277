package com.example.carecareforeldres.Entity;

import com.example.carecareforeldres.Repository.CuisinierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ScoreWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CuisinierRepository cuisinierRepository;

    @MessageMapping("/updateScore")
    public void updateScore() {
        List<Cuisinier> top2 = cuisinierRepository.findTop2ByOrderByScoreDesc();
        messagingTemplate.convertAndSend("/topic/scores", top2);
    }
}
