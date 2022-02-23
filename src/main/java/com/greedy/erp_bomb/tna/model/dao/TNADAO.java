package com.greedy.erp_bomb.tna.model.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.greedy.erp_bomb.member.model.dto.MemberDTO;
import com.greedy.erp_bomb.tna.model.dto.TNADTO;
import com.greedy.erp_bomb.tna.model.dto.TNAPk;

@Repository
public class TNADAO {

	@PersistenceContext
	private EntityManager em;

	public List<TNADTO> tnaDateSearch(String find) {				// 근태내역 조회
		String jpql = null;
		List<TNADTO> data = null;
		
		if(find.equals("0")) {										// 전체 내역 조회
			jpql = "SELECT a FROM TNADTO as a ORDER BY a.date desc";
			data = em.createQuery(jpql, TNADTO.class).getResultList();
		} else {													// 특정 일자 조회
			jpql = "SELECT a FROM TNADTO as a WHERE a.date = :find ORDER BY a.date desc";
			data = em.createQuery(jpql, TNADTO.class).setParameter("find", find).getResultList();
		}
		
		return data;
	}

	public List<TNADTO> selectDetail(String name) {					// 특정 인원 근태조회
		String jpql = "SELECT a FROM TNADTO as a WHERE a.member.name = :name";
		
		List<TNADTO> data = em.createQuery(jpql, TNADTO.class).setParameter("name", name).getResultList();
		
		return data;
	}

	public void regiTna(TNADTO tna) {								// 근태 내역 수정
		String jpql = "SELECT a FROM TNADTO as a WHERE a.member.name = :name AND a.date = :date";
		
		TNADTO regiTna = em.createQuery(jpql, TNADTO.class).setParameter("name", tna.getMember().getName()).setParameter("date", tna.getDate()).getSingleResult();
		
		regiTna.setCode(tna.getCode());
		
	}

	public void deletWork(TNADTO tna) {								// 근태 내역 삭제
		
		TNAPk tnaPK = new TNAPk();									// 삭제할 내역의 PK 제약조건에 값을 옮김
		tnaPK.setDate(tna.getDate());
		tnaPK.setMember(tna.getMember().getName());
		
		TNADTO deletMember = em.find(TNADTO.class, tnaPK);
		
		em.remove(deletMember);
		
	}

	public void newWork(TNADTO tna) {								// 근태 내역 추가
		
		MemberDTO member = em.find(MemberDTO.class, tna.getMember().getName());
		
		tna.setMember(member);
		
		em.persist(tna);
	}
}
