package com.multi.semi03.pharmacy.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.multi.semi03.common.util.PageInfo;
import com.multi.semi03.member.model.vo.Member;
import com.multi.semi03.pharmacy.model.service.PharmService;
import com.multi.semi03.pharmacy.model.vo.FavorPharmacy;
import com.multi.semi03.pharmacy.model.vo.Pharmacy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class PharmacyController {
	
	@Autowired
	private PharmService service;
	
	
	@RequestMapping("/pharm/search")
	public String Search(Model model,
			HttpSession session,
			@RequestParam Map<String, Object> paramMap,
			@RequestParam(required = false) String sido,
			@RequestParam(required = false) String gugun,
			@RequestParam(required = false) String address,
			HttpServletRequest request
			) {
		int page = 1;
		log.info("page : " + page);
		log.info("@@@@@@@@@@@@" + paramMap);

		if(paramMap.get("page") != null) {
			try {
				page = Integer.parseInt((String)paramMap.get("page"));
			} catch(Exception e) {}
		}
		
		if(sido != null && sido.length() > 0) {
			paramMap.put("sido", sido);
		}else {
			paramMap.remove("sido");
		}
		
		if(gugun != null && gugun.length() > 0) {
			paramMap.put("gugun", gugun);
		}else {
			paramMap.remove("gugun");
		}
	
		if(address != null && address.length() > 0) {
			paramMap.put("address", address);
		}else {
			paramMap.remove("address");
		}
		
		int count = service.getPharmacyCount(paramMap);
		PageInfo pageInfo = new PageInfo(page, 5, count, 5);
		List<Pharmacy> list = service.getPharmacyList(pageInfo, paramMap);
		int cnt = 1;
		for(Pharmacy item : list) {
			item.setPharmImg(request.getContextPath() +"/resources/img/pharmacyImg/"+"pharmacyImg"+cnt +".png");
			cnt++;
		}
		model.addAttribute("count", count);
		model.addAttribute("list", list);
		model.addAttribute("pageInfo", pageInfo);
		model.addAttribute("paramMap", paramMap);
		
		if(session.getAttribute("loginMember") != null) {
			Member member = (Member) session.getAttribute("loginMember");
			int mNO = member.getMno();
			List<FavorPharmacy> favorList = service.getFavorPharmacyList(mNO);
			model.addAttribute("favorList", favorList);
		}

		return "pharmacy/pharmSearch";
	}
	
	@ResponseBody
	@PostMapping(value = "/pharm/addFavor", produces = "application/json;charset=UTF-8")
	public ResponseEntity<Map<String, String>> addFavor(HttpSession session,
			@RequestBody Map<String, Object> request){
		  Member member = (Member) session.getAttribute("loginMember");
		    if (member == null) {
		        Map<String, String> response = new HashMap<>();
		        response.put("message", "로그인이 필요합니다.");
		        return ResponseEntity.badRequest().body(response);
		    }

		    int mno = member.getMno();
		    int dutyNo = Integer.parseInt(request.get("dutyNo").toString());

		    Map<String, Object> map = new HashMap<>();
		    map.put("mno", mno);
		    map.put("dutyNo", dutyNo);

		    if (service.getFavorCount(map) > 0) {
		        service.removeFavor(map);
		        Map<String, String> response = new HashMap<>();
		        response.put("message", "이미 즐겨찾기에 추가되어 있습니다. 삭제합니다.");

		        response.put("bookmarked", "true");

		        return ResponseEntity.ok(response);
		    } else {
		        if (service.addFavor(map) > 0) {
		            Map<String, String> response = new HashMap<>();
		            response.put("message", "즐겨찾기에 추가되었습니다.");

		            response.put("bookmarked", "false");

		            return ResponseEntity.ok(response);
		        } else {
		            Map<String, String> response = new HashMap<>();
		            response.put("message", "추가하지 못했습니다. 다시 확인해 주세요.");
		            return ResponseEntity.ok(response);
		        }
		    }
	}
	
	@RequestMapping("/pharmacy/FavorPharm")
	public String FavorPharmacy(Model model,HttpSession session,
								HttpServletRequest request) {
		Member member =(Member) session.getAttribute("loginMember");
		if(member == null) {
			model.addAttribute("msg","로그인이 필요합니다.");
			model.addAttribute("location","/");
			return "common/msg";
		}
		int mno = member.getMno();
		
		List<FavorPharmacy> favorList = service.getFavorList(mno);
		List<Pharmacy> list = service.getPharmacyByFavorList(favorList);
		int cnt = 1;
		for(Pharmacy item : list) {
			item.setPharmImg(request.getContextPath() +"/resources/img/pharmacyImg/"+"pharmacyImg"+cnt +".png");
			cnt++;
		}
		log.info("favorList" + favorList);
		log.info("Pharmacy" + list);
		model.addAttribute("list", list);
		return "pharmacy/FavorPharm";
	}
}

