package com.greedy.erp_bomb.vote.model.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.greedy.erp_bomb.common.paging.SelectCriteria;
import com.greedy.erp_bomb.member.model.dto.MemberDTO;
import com.greedy.erp_bomb.vote.model.dto.VoteDTO;
import com.greedy.erp_bomb.vote.model.dto.VoteOptionDTO;
import com.greedy.erp_bomb.vote.model.dto.VoteParticipationDTO;

@Repository
public class VoteDAO {

	@PersistenceContext	
	private EntityManager em;

	public List<VoteDTO> selectALLVote(){											// 투표 게시판 전체 조회
		
		String jpql = "SELECT a FROM VoteDTO as a ORDER BY a.serialNo DESC";
		
		TypedQuery<VoteDTO> query = em.createQuery(jpql, VoteDTO.class);
		
		List<VoteDTO> voteList = query.getResultList();
		
		return voteList;
	}
	
	public void insertVote(VoteDTO vote) {											// 후보 없이 신규 투표 작성
		em.persist(vote);						
	}

	public void insertVote(VoteOptionDTO voteOption) {								// 후보 있는 신규 투표 작성
		voteOption.getVote().getVoteOptionList().add(voteOption);
		voteOption.setMember(em.find(MemberDTO.class, voteOption.getVote().getMember().getName()));
		
		em.persist(voteOption);
	}

	public VoteDTO selectVoteDetail(int detailnum) {								// 투표 상세 조회
		
		VoteDTO voteDetail = em.find(VoteDTO.class, detailnum);
		
		int hit = voteDetail.getHit();												// 조회수
		voteDetail.setHit(hit+1);
		
		em.persist(voteDetail);														// 조회수 증가
		
		voteDetail.getVoteOptionList().size();
		voteDetail.getVoteParticipationList().size();
		
		return voteDetail;
	}

	public void insertVvote(VoteParticipationDTO vote, String desc, int serialNo) {		// 투표하기
		
		vote.setVote(em.find(VoteDTO.class, serialNo));									// 선택한 투표 게시판
		vote.setMember(em.find(MemberDTO.class, vote.getMember().getName()));			// 투표하는 계정의 이름
		
		vote.getVote().setVoteOptionList(null);
		vote.getVote().setVoteParticipationList(null);
		
		/* 투표할 후보의 이름과 게시판 번호로 조회 */
		TypedQuery<VoteOptionDTO> query = em.createQuery("SELECT a FROM VoteOptionDTO as a WHERE a.desc = :desc AND a.vote.serialNo = :serialNo", VoteOptionDTO.class);
		
		query.setParameter("desc", desc);
		query.setParameter("serialNo", serialNo);
		
		VoteOptionDTO voteOption = query.getSingleResult();
		
		int num = voteOption.getVoteCount();
		
		voteOption.setVoteCount(num + 1);												// 선택한 후보의 Count 증가
		
		em.persist(vote);
	}

	public VoteDTO selectResult(int voteNumber) {										// 중간결과 조회
		
		VoteDTO result = em.find(VoteDTO.class, voteNumber);							// 해당 투표게시판의 정보를 가져옴
		
		result.getVoteOptionList().size();
		result.setVoteParticipationList(null);
		
		return result;
	}

	public void insertCandidate(VoteOptionDTO voteOption) {										// 후보 추가
		
		voteOption.setMember(em.find(MemberDTO.class, voteOption.getMember().getName()));		// DB에 저장되어있는 직원인지 조회하여 DTO에 저장
		voteOption.setVote(em.find(VoteDTO.class, voteOption.getVote().getSerialNo()));			// 투표 게시판의 정보를 조회하여 변수에 DTO에 저장
		
		em.persist(voteOption);																	// DTO에 담긴 값을 DB로 persist
	}
	
	
}
