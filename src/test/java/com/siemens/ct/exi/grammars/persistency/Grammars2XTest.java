package com.siemens.ct.exi.grammars.persistency;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.core.grammars.SchemaInformedGrammars;
import com.siemens.ct.exi.core.helpers.DefaultEXIFactory;
import com.siemens.ct.exi.grammars.XSDGrammarsBuilder;
import com.siemens.ct.exi.grammars._2017.schemaforgrammars.ExiGrammars;
import com.siemens.ct.exi.main.api.sax.EXIResult;
import com.siemens.ct.exi.main.api.sax.EXISource;

public class Grammars2XTest extends TestCase {

	XSDGrammarsBuilder grammarBuilder = XSDGrammarsBuilder.newInstance();

	public Grammars2XTest() throws EXIException {
		super();
	}

	protected void _test(String xsd, String xml) throws Exception {
		grammarBuilder.loadGrammars(xsd);
		SchemaInformedGrammars grammarsIn = grammarBuilder.toGrammars();

		Grammars2X g2X = new Grammars2X();
		ExiGrammars exiGrammarIn = g2X.toGrammarsX(grammarsIn);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Grammars2X.marshal(exiGrammarIn, new StreamResult(baos));
		// System.out.println(new String(baos.toByteArray()));

		ExiGrammars exiGrammarOut = Grammars2X
				.unmarshal(new ByteArrayInputStream(baos.toByteArray()));

		// further validation
		SchemaInformedGrammars grammarsOut = Grammars2X
				.toGrammars(exiGrammarOut);

		// try to encode with original Grammars and to decode with transformed
		// Grammars
		EXIFactory exiFactory = DefaultEXIFactory.newInstance();
		// exiFactory.setFidelityOptions(FidelityOptions.createAll()); // for
		// XML equal test

		// 1. Encode
		exiFactory.setGrammars(grammarsIn);

		ByteArrayOutputStream osEXI = new ByteArrayOutputStream();
		EXIResult exiResult = new EXIResult(exiFactory);
		exiResult.setOutputStream(osEXI);
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(exiResult.getHandler());
		InputSource isXML = new InputSource(xml);
		xmlReader.parse(isXML); // parse XML input
		osEXI.close();

		// 2. Decode
		exiFactory.setGrammars(grammarsOut);
		ByteArrayOutputStream osXML = new ByteArrayOutputStream();
		Result result = new StreamResult(osXML);
		SAXSource exiSource = new EXISource(exiFactory);
		exiSource.setInputSource(new InputSource(new ByteArrayInputStream(osEXI
				.toByteArray())));
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.transform(exiSource, result);
	}

	@Test
	public void testNotebook() throws Exception {
		String xsd = "data/W3C/PrimerNotebook/notebook.xsd";
		String xml = "data/W3C/PrimerNotebook/notebook.xml";
		_test(xsd, xml);
	}

	@Test
	public void testEXIForJSON_02() throws Exception {
		String xsd = "data/W3C/EXIforJSON/exi4json.xsd";
		String xml = "data/W3C/EXIforJSON/02.json.xml";
		_test(xsd, xml);
	}

	@Test
	public void testEXIForJSON_Fstab1() throws Exception {
		String xsd = "data/W3C/EXIforJSON/exi4json.xsd";
		String xml = "data/W3C/EXIforJSON/fstab1.json.xml";
		_test(xsd, xml);
	}

	@Test
	public void testDatatypes() throws Exception {
		String xsd = "data/general/datatypes.xsd";
		String xml = "data/general/datatypes.xml";
		_test(xsd, xml);
	}

	@Test
	public void testPull5() throws Exception {
		// very special case: same attribute with different type --> represented
		// as a String (default string)
		String xsd = "data/general/pull5.xsd";
		String xml = "data/general/pull5.xml";
		_test(xsd, xml);
	}

	protected void testRoundTripExiGrammarsXml(String xsd) throws Exception {
		grammarBuilder.loadGrammars(xsd);
		SchemaInformedGrammars grammarsIn = grammarBuilder.toGrammars();

		Grammars2X grammars2X = new Grammars2X();
		ExiGrammars exiGrammars = grammars2X.toGrammarsX(grammarsIn);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Grammars2X.marshal(exiGrammars, new StreamResult(baos));
		String exiGrammarsXml = baos.toString();

		ExiGrammars exiGrammarOut = Grammars2X
				.unmarshal(new ByteArrayInputStream(baos.toByteArray()));
		SchemaInformedGrammars grammarsOut = Grammars2X
				.toGrammars(exiGrammarOut);

		grammars2X.clear();
		ExiGrammars exiGrammarsRoundTrip = grammars2X.toGrammarsX(grammarsOut);
		ByteArrayOutputStream baosRoundTrip = new ByteArrayOutputStream();
		Grammars2X.marshal(exiGrammarsRoundTrip,
				new StreamResult(baosRoundTrip));
		String exiGrammarsXmlRoundTrip = baosRoundTrip.toString();

		XMLAssert.assertXMLEqual(exiGrammarsXml, exiGrammarsXmlRoundTrip);
	}

	@Test
	public void testRoundTripExiGrammarsXmlNotebook() throws Exception {
		testRoundTripExiGrammarsXml("data/W3C/PrimerNotebook/notebook.xsd");
	}

	@Test
	public void testRoundTripExiGrammarsGaml100() throws Exception {
		testRoundTripExiGrammarsXml("data/Gaml/gaml100.xsd");
	}
}
