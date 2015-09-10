package mia.lette.com.museum;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class XmlParser extends DefaultHandler {
	// We don't use namespaces
	private static final String ns = null;
	List<Quest> p_quests = null;
	List<Quest> t_quests = null;
	String[] a = new String[10];


	public List<Quest> parse(InputStream in, String pt) throws XmlPullParserException,
			IOException {
	
//		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//        StringBuilder out = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            out.append(line);
//        }
//        Log.i("haha",out.toString());
//        reader.close();

		try {
			XmlPullParser parser = Xml.newPullParser();	
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);	
			parser.setInput(in, null);
			parser.next();
			p_quests = new ArrayList<Quest>();
			t_quests = new ArrayList<Quest>();
			parser.require(XmlPullParser.START_TAG, ns, "Quests");

			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}
				String name = parser.getName();

				Log.i("haha", "huhu " + parser.getName());
				// Starts by looking for the entry tag

				if (name.equals("PuzzleCollection")) {

					p_quests = readPuzzleCollection(parser);
				} else if (name.equals("TimeCollection")) {
					t_quests = readTimeCollection(parser);

				} else {
					skip(parser);
				}
			}
			
		} finally {
			in.close();
		}
		
		if (pt.equals("p")) return p_quests;
		else if (pt.equals("t")) return t_quests;
		else return null;
	}
	
	
	private List<Quest> readPuzzleCollection(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "PuzzleCollection");
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			Log.i("haha", "...huhu " + parser.getName());
			Log.i("haha", "name Quest was drin steht: "+ name.equals("Quest"));
			if (name.equals("Quest")) {
				Log.i("haha", p_quests.size() + "...huhu " + parser.getDepth());
				try {

					p_quests.add(readQuest(parser));
					Log.i("haha", "steht was in p_quests? " + p_quests.get(0).answer4);
				} catch (IOException e) {
					Log.i("haha", "falsch..." + e);
				}
			}  else {
				skip(parser);
			}
		}
				
		return p_quests;
	}
	
	private List<Quest> readTimeCollection(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "TimeCollection");
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			Log.i("haha", "...huhu " + parser.getName());
			Log.i("haha", "name Quest was drin steht: "+ name.equals("Quest"));
			if (name.equals("Quest")) {
				Log.i("haha", t_quests.size() + "...huhu " + parser.getDepth());
				try {
					Log.i("haha", "kommt er rein?");
					t_quests.add(readQuest(parser));
					Log.i("haha", "Inhalt" + t_quests);
				} catch (IOException e) {
					Log.i("haha", "falsch..." + e);
				}
			}  else {
				skip(parser);
			}
		}
				
		return t_quests;
	}

	// Parses the contents of an entry. If it encounters a title, summary, or
	// link tag, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the
	// tag.
	private Quest readQuest(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Quest");
		String xName = parser.getAttributeValue(null, "name");
		Log.i("haha", "Name:" + a[0]);


		Log.i("haha", "Wieso? " + xName);
		String[] separated =  xName.split("[.]");
		String questionType = new String();
		String question = new String();
if(xName.length() >= 2) {
	question = separated[0];
	Log.i("haha", "sadasdasdasdasdasda" + separated[1]);
	questionType = separated[1];
}

		String xTitle = parser.getAttributeValue(null, "title");
		//String xType = parser.getAttributeValue(null, "type");
		String xOk = parser.getAttributeValue(null, "ok");

		String xA1 = null;
		String xA2 = null;
		String xA3 = null;
		String xA4 = null;

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			//String name = parser.getName();
			Log.i("haha", "......huhu " + parser.getName());
			if (name.equals("Answer1")) {
				xA1 = readAnswer1(parser);
			} else if (name.equals("Answer2")) {
				xA2 = readAnswer2(parser);
			} else if (name.equals("Answer3")) {
				xA3 = readAnswer3(parser);
			} else if (name.equals("Answer4")) {
				xA4 = readAnswer4(parser);
			} else {
				//skip(parser);
			}
		}
		
		//Log.i("haha", xName + "" + xTitle + "" + xA1 + "" + xA2 + "" + xA3 + "" + xType + "" + xOk);
		Log.i("haha", question + "" + xTitle + "" + xA1 + "" + xA2 + "" + xA3 + "" + questionType + "" + xOk);
		return new Quest(question, xTitle, xA1, xA2, xA3, xA4, questionType, xOk);

	}

	// Processes Answer1 tags in the Quest.
	private String readAnswer1(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "Answer1");
		String answer1 = readText(parser);
		Log.i("haha", ".........huhu " + answer1);
		parser.require(XmlPullParser.END_TAG, ns, "Answer1");
		return answer1;
	}

	// Processes Answer2 tags in the Quest.
	private String readAnswer2(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "Answer2");
		String answer2 = readText(parser);
		Log.i("haha", ".........huhu " + answer2);
		parser.require(XmlPullParser.END_TAG, ns, "Answer2");
		return answer2;
	}

	// Processes Answer3 tags in the Quest.
	private String readAnswer3(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "Answer3");
		String answer3 = readText(parser);
		Log.i("haha", ".........huhu " + answer3);
		parser.require(XmlPullParser.END_TAG, ns, "Answer3");
		return answer3;
	}

	// Processes Answer4 tags in the Quest.
	private String readAnswer4(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "Answer4");
		String answer4 = readText(parser);
		Log.i("haha", ".........huhu " + answer4);
		parser.require(XmlPullParser.END_TAG, ns, "Answer4");
		return answer4;
	}

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

}
