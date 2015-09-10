package mia.lette.com.museum;

import java.io.Serializable;

public class Quest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String name;
	public String title;
	public String answer1;
	public String answer2;
	public String answer3;
	public String answer4;
	public String type;
	public String ok;

	public Quest(String name, String question, String answer1, String answer2,
			String answer3, String answer4, String type, String ok) {

		this.name = name;
		this.title = question;
		this.answer1 = answer1;
		this.answer2 = answer2;
		this.answer3 = answer3;
		this.answer4 = answer4;
		this.type = type;
		this.ok = ok;

	}

	public Quest(String name, String question, String type, String ok) {

		this.name = name;
		this.title = question;
		this.type = type;
		this.ok = ok;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAnswer1() {
		return answer1;
	}

	public void setAnswer1(String answer1) {
		this.answer1 = answer1;
	}

	public String getAnswer2() {
		return answer2;
	}

	public void setAnswer2(String answer2) {
		this.answer2 = answer2;
	}

	public String getAnswer3() {
		return answer3;
	}

	public void setAnswer3(String answer3) {
		this.answer3 = answer3;
	}

	public String getAnswer4() {
		return answer4;
	}

	public void setAnswer4(String answer4) {
		this.answer4 = answer4;
	}

	public String getOk() {
		return ok;
	}

	public void setSolution(String ok) {
		this.ok = ok;
	}

}
