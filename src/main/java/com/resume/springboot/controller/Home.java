package com.resume.springboot.controller;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.resume.springboot.domain.Owner;
import com.resume.springboot.domain.Repos;

@Controller
public class Home {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(Model model) {

		model.addAttribute("owner", new Owner());

		return "index";
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String addNewPost(@Valid Owner owner, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		Map<String, Integer> resume;

		if (bindingResult.hasErrors()) {
			return "index";
		}

		if (checkUserExistence(owner.getLogin())) {
			resume = getResumeData(owner.getLogin());
			if (resume.isEmpty())
				return "redirect:/not_found";
		} else
			return "redirect:/not_found";

		redirectAttributes.addFlashAttribute("language", resume);
		redirectAttributes.addFlashAttribute("username", owner.getLogin());
		return "redirect:/result";
	}

	@RequestMapping(value = "/result", method = RequestMethod.GET)
	public String showResult() {
		return "result";
	}

	@RequestMapping(value = "/not_found", method = RequestMethod.GET)
	public String showNotFound() {
		return "not_found";
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Map<String, Integer>> getResume(
			@PathVariable("name") String name, Model model) {

		Map<String, Integer> resume = new HashMap<>();

		if (checkUserExistence(name)) {
			resume = getResumeData(name);
		}

		return new ResponseEntity<Map<String, Integer>>(resume, HttpStatus.OK);
	}

	public Map<String, Integer> getResumeData(String name)
			throws HttpClientErrorException {

		Map<String, Integer> resume = new HashMap<String, Integer>();
		RestTemplate restTemplate = new RestTemplate();
		Repos[] repos = null;
		String url = "https://api.github.com/users/" + name
				+ "/repos?per_page=1000";
		repos = restTemplate.getForObject(url, Repos[].class);

		for (Repos re : repos) {
			if (re.getLanguage() != null
					&& !re.getLanguage().equalsIgnoreCase("null"))

				if (resume.containsKey(re.getLanguage())) {
					resume.put(re.getLanguage(),
							resume.get(re.getLanguage()) + 1);
				} else {
					resume.put(re.getLanguage(), 1);
				}
		}

		return resume;

	}

	private boolean checkUserExistence(String name) {
		RestTemplate restTemplate = new RestTemplate();
		Owner owner = null;
		try {
			String url = "https://api.github.com/users/" + name;
			owner = restTemplate.getForObject(url, Owner.class);
		} catch (HttpClientErrorException e) {
			return false;
		}

		return true;

	}
}