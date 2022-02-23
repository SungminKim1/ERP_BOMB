package com.greedy.erp_bomb.tna.contorller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.greedy.erp_bomb.inventory.model.dto.CompanyDTO;
import com.greedy.erp_bomb.member.model.dto.DeptDTO;
import com.greedy.erp_bomb.member.model.dto.MemberDTO;
import com.greedy.erp_bomb.member.model.dto.RankDTO;
import com.greedy.erp_bomb.tna.model.dto.TNADTO;
import com.greedy.erp_bomb.tna.model.service.TNAService;

@Controller
@RequestMapping("/admin")
public class TNAContorller {
	
	private TNAService tnaService;
	
	@Autowired
	public TNAContorller(TNAService tnaService) {
		this.tnaService = tnaService;
	}
	
	@GetMapping("/tna")
	public String regTna() {
		return "/admin/regTna";
	}
	
	@GetMapping("/tnaDetail")
	public void tnaDetail() {}
	
	@GetMapping(value = "/tnaList", produces = "application/json; charset=UTF-8")    	// Data Tables(API)를 ajax로 조회하여 json 문자열 변환
	@ResponseBody
	public List<TNADTO> dateSearch(@RequestParam String find){
		
		List<TNADTO> data = tnaService.tnaDateSearch(find);
		/* 
		   첫 페이지 조회시 전체 목록 조회 및 선택한 날을 조회하기 위한 변수(find)
		   처음 받는 값은 0으로 하여 DAO에서 문자열 비교를 통해 전체 조회 또는 특정 날짜를 조회해온다.
		*/
		
		for (TNADTO tna : data) {
			String name = tna.getMember().getName();					// 직원 이름
			int number = tna.getMember().getEmpNo();					// 사번
			String dept = tna.getMember().getDept().getName();			// 부서명
			String company = tna.getMember().getCompany().getName();	// 지부명
			String rank = tna.getMember().getRank().getName();			// 직급명
			
			if(tna.getCode() == 1) {									// 근태코드 - 코드 번호에 따라 근태상황이 처리된다.
				tna.setStatus("출근");
			} else if(tna.getCode() == 2) {
				tna.setStatus("지각");
			} else if(tna.getCode() == 3) {
				tna.setStatus("조퇴");
			} else if(tna.getCode() == 4) {
				tna.setStatus("결근");
			}
			
			DeptDTO dep = new DeptDTO();
			dep.setName(dept);
			
			RankDTO ran = new RankDTO();
			ran.setName(rank);
			
			CompanyDTO comp = new CompanyDTO();
			comp.setName(company);

			MemberDTO member = new MemberDTO();
			member.setName(name);
			member.setEmpNo(number);
			member.setRank(ran);
			member.setCompany(comp);
			member.setDept(dep);
			
			tna.setMember(null);
			tna.setMember(member);
			
			// front로 보내기 위해 TNADTO에 조회해온 값을 담는다.
		}
		
		return data;
	}
	
	@GetMapping("/detail")		// 근태내역 상세 조회
	public ModelAndView detailMember(ModelAndView mv, @RequestParam String name) {
		
		List<TNADTO> member = tnaService.selectDetail(name);		// 선택한 인원의 내역 조회
		MemberDTO member1 = null;									// 근태내역 추가를 위한 부분(HTML)에서 사용하기 위한 부분
		for (TNADTO tna : member) {
			
			if(tna.getCode() == 1) {
				tna.setStatus("출근");
			} else if(tna.getCode() == 2) {
				tna.setStatus("지각");
			} else if(tna.getCode() == 3) {
				tna.setStatus("조퇴");
			} else if(tna.getCode() == 4) {
				tna.setStatus("결근");
			}
			
			member1 = tna.getMember();
		}
		
		mv.addObject("member", member);
		mv.addObject("mem", member1);
		
		mv.setViewName("/admin/tnaDetail");
		
		return mv;
	}
	
	@GetMapping("/regiTna")			// 근태내역 수정
	public ModelAndView regiTna(ModelAndView mv, @RequestParam String name, @RequestParam String date,
			@RequestParam int selectStat) {
		MemberDTO member = new MemberDTO();
		member.setName(name);
		
		TNADTO tna = new TNADTO();
		tna.setCode(selectStat);
		tna.setDate(date);
		tna.setMember(member);
		
		tnaService.regiTna(tna);
		
		mv.addObject("name", name);			// redirect로 detail 메소드로 보내기위한 매개변수
		
		mv.setViewName("redirect:/admin/detail");
		
		return mv;
	}
	
	@GetMapping("/deleteWork")			// 근태내역 삭제
	public ModelAndView deletWork(ModelAndView mv, @RequestParam String name, @RequestParam String date) {
		
		MemberDTO member = new MemberDTO();
		member.setName(name);
		
		TNADTO tna = new TNADTO();
		tna.setDate(date);
		tna.setMember(member);
		
		tnaService.deletWork(tna);
		
		mv.addObject("name", name);		// redirect로 detail 메소드로 보내기위한 매개변수
		
		mv.setViewName("redirect:/admin/detail");
		
		return mv;
	}
	
	@GetMapping("/newWork")			// 근태내역 추가
	public ModelAndView newWork(ModelAndView mv, @RequestParam String name, @RequestParam String date,
			@RequestParam int selectStat) {
		
		MemberDTO member = new MemberDTO();
		member.setName(name);
		
		TNADTO tna = new TNADTO();
		tna.setCode(selectStat);		// 근태 코드
		tna.setDate(date);				
		tna.setMember(member);
		
		tnaService.newWork(tna);
		
		mv.addObject("name", name);		// redirect로 detail 메소드로 보내기위한 매개변수
		
		mv.setViewName("redirect:/admin/detail");
		
		return mv;
	}
}
