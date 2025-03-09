package com.ftc.containerView.service;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LogService {

    private final FirebaseFirestore firestore;

    public LogService() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void registrarLog(String tipo, String mensagem) {
        Map<String, Object> log = new HashMap<>();
        log.put("tipo", tipo);
        log.put("mensagem", mensagem);
        log.put("timestamp", System.currentTimeMillis());

        CollectionReference logsRef = firestore.collection("logs");
        logsRef.add(log);
    }
}
