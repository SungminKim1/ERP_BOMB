package com.greedy.erp_bomb.ea.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greedy.erp_bomb.ea.model.dao.EaDAO;

@Service
public class EaService {
	
	private EaDAO eaDAO;
	
	@Autowired
	public EaService(EaDAO eaDAO) {
		this.eaDAO = eaDAO;
	}

}