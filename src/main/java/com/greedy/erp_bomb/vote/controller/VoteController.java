package com.greedy.erp_bomb.vote.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.greedy.erp_bomb.member.model.dto.MemberDTO;
import com.greedy.erp_bomb.member.model.dto.UserImpl;
import com.greedy.erp_bomb.vote.model.dto.VoteDTO;
import com.greedy.erp_bomb.vote.model.dto.VoteOptionDTO;
import com.greedy.erp_bomb.vote.model.dto.VoteParticipationDTO;
import com.greedy.erp_bomb.vote.model.service.VoteService;

@Controller
@RequestMapping("/vote/*")
public class VoteController {

	private VoteService voteService;
	
	public VoteController(VoteService voteService) {
		this.voteService = voteService;
	}
	
	@GetMapping(value = "vote")									// 투표 게시판
	public ModelAndView votePage(ModelAndView mv, @RequestParam(defaultValue = "1") String tab) {	
		
		List<VoteDTO> voteList = voteService.selectALLVote();
		
		Date date = new Date();									// 현재 시간으로 비교하기 위한 변수
		List<VoteDTO> endVoteList = new ArrayList<>();
		List<VoteDTO> regVoteList = new ArrayList<>();
		
		/* 진행중, 종료 부분을 위한 코딩 */
		for (VoteDTO voteDTO : voteList) {						// 투표의 종료일과 오늘 날짜를 비교
			if (voteDTO.getEndDate().before(date)) {			
				endVoteList.add(voteDTO);
			}
			if (voteDTO.getEndDate().after(date)) {
				regVoteList.add(voteDTO);
			}
		}
		
		mv.addObject("tab", tab);								// 전체, 진행중, 종료를 구분하기위한 값(HTML에서 표시를 다르게해줌)
		mv.addObject("voteList", voteList);						// 전체 게시글
		mv.addObject("endVoteList", endVoteList);				// 종료된 게시글
		mv.addObject("regVoteList", regVoteList);				// 진행중인 게시글
		
		mv.setViewName("/vote/vote");
		
		return mv;
	}
	
	@PostMapping("insertVote")									// 신규 투표
	public ModelAndView insertVote(ModelAndView mv, @RequestParam String title, @RequestParam String insertMember,
			@RequestParam java.sql.Date endDate, @RequestParam String content
			, @AuthenticationPrincipal UserImpl user) {			// 투표 작성시 받는 매개변수 각 변수의 클래스가 달라 @ModelAttribute 사용 안함
		
		/* 작성일 */
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		
		MemberDTO member = new MemberDTO();
		member.setName(user.getName());							// 현재 로그인한 직원의 이름
		
		VoteDTO vote = new VoteDTO();							// 투표 작성에 필요한 값 넣기
		vote.setMember(member);									// 로그인한 직원
		vote.setTitle(title);									// 투표 제목
		vote.setRegDate(date);									// 작성일
		vote.setEndDate(endDate);								// 종료일
		vote.setContent(content);								// 내용
		
		/* 신규 투표 작성시 후보가 있나없나 판별 */
		if(!insertMember.isEmpty()) {							// 신규 투표 작성시 후보를 등록하면서 작성
			VoteOptionDTO voteOption = new VoteOptionDTO();
			
			voteOption.setVote(vote);							// 투표 정보
			voteOption.setMember(member);						// 후보 등록하는 계정
			voteOption.setDesc(insertMember);					// 후보
			
			voteService.insertVote(voteOption);
		} else {
			voteService.insertVote(vote);						// 후보 등록을 하지 않고 투표 게시글만 작성
		}
		
		mv.setViewName("redirect:/vote/vote");
		
		return mv;
	}
	
	@GetMapping(value = "detail", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public VoteDTO voteDetail(@RequestParam int detailnum) {
		
		VoteDTO voteDetail = voteService.selectVoteDetail(detailnum);				// 선택한 투표 게시판의 정보 가져오기
		int[] voteEqual = new int[voteDetail.getVoteOptionList().size()];			// 해당 게시판의 후보의 수
		int max = 0;
		
		for(int i = 0; i < voteEqual.length; i++) {									// 최다 득표 후보를 판별하기 위한 반복문
			voteEqual[i] = voteDetail.getVoteOptionList().get(i).getVoteCount();
			if(max < voteEqual[i]){
                max = voteEqual[i];
            }
		}
		
		/* json 문자열 반환을 위해 DTO안의 List들을 끊어냄 */
		for (VoteOptionDTO vote : voteDetail.getVoteOptionList()) {
			String member = vote.getMember().getName();

			vote.setMember(null);
			vote.setVote(null);
			
			MemberDTO mem = new MemberDTO();
			mem.setName(member);
			
			vote.setMember(mem);
			
			if(max == vote.getVoteCount()) {
				vote.setTopVote(1);													// 최다 득표한 후보
			}
		}
		
		/* json 문자열 반환을 위해 DTO안의 List들을 끊어냄 */
		for (VoteParticipationDTO votePa : voteDetail.getVoteParticipationList()) {
			String member = votePa.getMember().getName();
			int voteNum = votePa.getVote().getSerialNo();
			
			votePa.setMember(null);
			votePa.setVote(null);
			
			MemberDTO mem = new MemberDTO();
			mem.setName(member);
			
			VoteDTO vote = new VoteDTO();
			vote.setSerialNo(voteNum);
			
			votePa.setMember(mem);
			votePa.setVote(vote);
		}
		
		String member = voteDetail.getMember().getName();
		MemberDTO mem = new MemberDTO();
		mem.setName(member);
		
		voteDetail.setMember(mem);
		
		return voteDetail;
	}
	
	/* 투표하기 */
	@PostMapping(value = "vvote", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public void vvote(@RequestParam int serialNo, @RequestParam String desc,
			@AuthenticationPrincipal UserImpl user) {
		
		MemberDTO member = new MemberDTO();
		member.setName(user.getName());
		
		VoteParticipationDTO vote = new VoteParticipationDTO();	
		
		vote.setMember(member);							
		voteService.insertVvote(vote, desc, serialNo);
	}
	
	@GetMapping(value = "resultVote", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public VoteDTO resultvote(@RequestParam int voteNumber) {					// 중간 결과 확인
		VoteDTO result = voteService.selectResult(voteNumber);
		
		/* json 문자열 반환을 위해 DTO안의 List들을 끊어냄 */
		for (VoteOptionDTO vote : result.getVoteOptionList()) {
			
			vote.setMember(null);
			vote.setVote(null);
		}
		
		result.setMember(null);
		
		return result;
	}
	
	@PostMapping(value = "plusCandi", produces = "application/json; charset=UTF-8")			
	@ResponseBody
	public void plusCandidate(@RequestParam String candiInsert, @RequestParam int votenum,
			@AuthenticationPrincipal UserImpl user) {						// 후보 추가
		
		MemberDTO member = new MemberDTO();
		member.setName(user.getName());										// 후보를 추가하려는 계정의 이름
		
		VoteDTO vote = new VoteDTO();
		vote.setSerialNo(votenum);											// 추가할 투표 게시판의 번호
		
		VoteOptionDTO voteOption = new VoteOptionDTO();
		voteOption.setMember(member);
		voteOption.setVote(vote);
		voteOption.setDesc(candiInsert);									// 후보의 이름
		
		voteService.insertCandidate(voteOption);
	}
}