package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorContainerTools;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * <p>
 * Title: WekaConversion
 * </p>
 *
 * <p>
 * Description: helper tools that ease the use of WEKA data structures, i.e.,
 * Instances and Instance objects.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016-2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.06
 */
public class WekaConversion {

	public static Instances getInstances(ComplexDataContainer container) {
		if (container == null)
			return null;

		List<Attribute> attrs = new ArrayList<Attribute>();
		Map<String, Attribute> attributeMap = new HashMap<>();

		int dims = container.getAttributeNames().size();
		if (!container.getAttributeNames().contains("Name")) {
			dims++;
			Attribute a = new Attribute("Name", (List<String>) null);
			attrs.add(a);
			attributeMap.put("Name", a);
		}
		if (!container.getAttributeNames().contains("Description")) {
			dims++;
			Attribute a = new Attribute("Description", (List<String>) null);
			attrs.add(a);
			attributeMap.put("Description", a);
		}

		for (Iterator<String> iterator = container.getAttributeNames().iterator(); iterator.hasNext();) {
			String string = iterator.next();

			Attribute a = null;
			if (container.isNumeric(string))
				a = new Attribute(string);
			else if (container.isBoolean(string))
				a = new Attribute(string);
			else
				a = new Attribute(string, (List<String>) null);
			attrs.add(a);
			attributeMap.put(string, a);
		}

		Instances instances = new Instances("ComplexDataContainer " + container.toString(), (ArrayList<Attribute>) attrs, container.size());

		// create instance objects
		for (ComplexDataObject cdo : container) {
			Instance instance = new DenseInstance(dims);

			Iterator<String> attNames = cdo.iterator();
			while (attNames.hasNext()) {
				String attName = attNames.next();
				Attribute attribute = attributeMap.get(attName);

				Object value = cdo.getAttribute(attName);
				if (container.isNumeric(attName)) {
					if (value != null)
						instance.setValue(attribute, ((Number) value).doubleValue());
				} else if (container.isBoolean(attName)) {
					if (value != null) {
						Integer i = ((Boolean) value).booleanValue() ? 1 : 0;
						instance.setValue(attribute, ((Number) i).doubleValue());
					}
				} else if (value != null)
					instance.setValue(attribute, value.toString());
			}

			Attribute nameAttribute = attributeMap.get("Name");
			instance.setValue(nameAttribute, cdo.getName());

			Attribute descripptionAttribute = attributeMap.get("Description");
			instance.setValue(descripptionAttribute, cdo.getDescription());

			instances.add(instance);
		}

		return instances;
	}

	/**
	 * 
	 * @param fvs
	 * @return
	 * @deprecated use
	 */
	public static <O extends Object, F extends Feature<O>, FV extends AbstractFeatureVector<O, F>> Instances getInstances(FeatureContainer<FV> featureContainer) {

		List<Attribute> attrs = new ArrayList<Attribute>(featureContainer.getFeatureNames().size());
		for (String featureName : featureContainer.getFeatureNames()) {
			Attribute a = null;
			if (featureContainer.isNumeric(featureName))
				a = new Attribute(featureName);
			else
				a = new Attribute(featureName, (List<String>) null);
			attrs.add(a);
		}

		Instances instances = new Instances("asdf", (ArrayList<Attribute>) attrs, featureContainer.size());
		addInstances(featureContainer, instances);
		return instances;
	}

	/**
	 * 
	 * @param featureContainer
	 * @param stringToNominal
	 * @return
	 */
	public static <O extends Object, F extends Feature<O>, FV extends AbstractFeatureVector<O, F>> Instances getInstances(FeatureContainer<FV> featureContainer, boolean stringToNominal) {

		List<Attribute> attributes = createAttributes(FeatureVectorContainerTools.getObjectList(featureContainer), stringToNominal);

		// List<Attribute> attrs = new
		// ArrayList<Attribute>(featureContainer.getFeatureNames().size());
		// for (String featureName : featureContainer.getFeatureNames()) {
		// Attribute a = null;
		// if (featureContainer.isNumeric(featureName))
		// a = new Attribute(featureName);
		// else
		// a = new Attribute(featureName, (List<String>) null);
		// attrs.add(a);
		// }

		Instances instances = new Instances("asdf", (ArrayList<Attribute>) attributes, featureContainer.size());

		addInstances(featureContainer, instances);

		return instances;
	}

	/**
	 * 
	 * @param fvs
	 * @param stringToNominal
	 *            decides whether string values are represented as nominal
	 *            values (with a concrete alphabet of observations)
	 * @return
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getInstances(List<FV> fvs, boolean stringToNominal) {
		List<Attribute> attrs = createAttributes(fvs, stringToNominal);

		Instances data = new Instances(fvs.get(0).getClass().getName(), (ArrayList<Attribute>) attrs, fvs.size());

		addInstances(fvs, data);

		return data;
	}

	/**
	 * creates a list of WEKA attributes for a given list of FVs.
	 * 
	 * @param fvs
	 * @param stringToNominal
	 * @return
	 */
	private static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> List<Attribute> createAttributes(List<FV> fvs, boolean stringToNominal) {
		if (fvs == null)
			return null;

		int length = fvs.get(0).getDimensions();
		List<Attribute> attributes = new ArrayList<Attribute>(length);

		for (int i = 0; i < length; i++) {
			Attribute a = null;
			if (fvs.get(0).getFeature(i).getFeatureType().equals(FeatureType.DOUBLE))
				a = new Attribute(i + 1 + "");
			else if (fvs.get(0).getFeature(i).getFeatureType().equals(FeatureType.BOOLEAN))
				a = new Attribute(i + 1 + "");
			else if (!stringToNominal)
				a = new Attribute(i + 1 + "", (List<String>) null);
			else {
				// collect alphabet
				SortedSet<String> alphabet = new TreeSet<>();
				for (FV fv : fvs)
					if (fv.getDimensions() == length)
						if (fv.getFeature(i) != null && fv.getFeature(i).getFeatureValue() != null)
							alphabet.add(fv.getFeature(i).getFeatureValue().toString());
						else {
							Feature<O> feature = fv.getFeature(fvs.get(0).getFeature(i).getFeatureName());
							if (feature != null && feature.getFeatureValue() != null)
								alphabet.add(feature.getFeatureValue().toString());
						}
				a = new Attribute(i + 1 + "", new ArrayList<>(alphabet));
			}
			attributes.add(a);
		}

		return attributes;
	}

	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> void addInstances(List<FV> fvs, Instances data) {
		if (fvs == null || fvs.size() == 0)
			return;

		int dim = fvs.get(0).getVectorRepresentation().size();
		for (FV fv : fvs) {
			int length = fv.getVectorRepresentation().size();
			if (dim != length)
				throw new IllegalArgumentException("List of input FV has different features.");

			data.add(new DenseInstance(length));
			Instance ins = data.get(data.size() - 1);

			fillInstanceByIndex(ins, fv, length);
		}
	}

	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> void addInstances(FeatureContainer<FV> featureContainer, Instances instances) {
		if (featureContainer == null || featureContainer.size() == 0)
			return;

		for (FV fv : featureContainer) {

			instances.add(new DenseInstance(fv.getDimensions()));
			Instance ins = instances.get(instances.size() - 1);

			fillInstanceByIndex(ins, fv, fv.getDimensions());
		}
	}

	public static void addMixedInstances(List<MixedDataFeatureVector> mfvs, Instances data) {
		for (MixedDataFeatureVector mfv : mfvs) {
			int length = mfv.getVectorRepresentation().size();

			data.add(new DenseInstance(length));

			Instance ins = data.get(data.size() - 1);

			fillInstanceByIndex(ins, mfv, length);
		}
	}

	public static Instances getLabeledInstancesNumerical(List<NumericalFeatureVector> fvs, String classAttribute) {
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < fvs.size(); i++)
			if (fvs.get(i).getAttribute(classAttribute) instanceof String)
				labels.add((String) fvs.get(i).getAttribute(classAttribute));
			else
				labels.add(fvs.get(i).getAttribute(classAttribute).toString());

		Instances insances = getInstances(fvs, false);

		return addLabelsToInstances(insances, labels);
	}

	/**
	 *
	 * @param fvs
	 * @param classAttribute
	 * @return
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getLabeledInstances(List<FV> fvs, String classAttribute) {

		return getLabeledInstances(fvs, null, classAttribute);
	}

	/**
	 * creates instances with weights for a given List of FVs.
	 * 
	 * @param fvs
	 * @param weights
	 * @param classAttribute
	 * @return
	 */
	public static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> Instances getLabeledInstances(List<FV> fvs, List<Double> weights, String classAttribute) {
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < fvs.size(); i++)
			if (fvs.get(i).getAttribute(classAttribute) == null)
				throw new IllegalArgumentException("WekaConverter.getLabeledInstances: classAttribute not found for given FeatureVector.");
			else if (fvs.get(i).getAttribute(classAttribute) instanceof String)
				labels.add((String) fvs.get(i).getAttribute(classAttribute));
			else
				labels.add(fvs.get(i).getAttribute(classAttribute).toString());

		Instances insances = getInstances(fvs, false);

		if (insances != null && weights != null && weights.size() == insances.size())
			addWeightsToInstances(insances, weights);

		return addLabelsToInstances(insances, labels);
	}

	/**
	 * Uses WEKAs ability to assign weights to Instances.
	 * 
	 * @param instances
	 * @param weights
	 * @return
	 */
	private static Instances addWeightsToInstances(Instances instances, List<Double> weights) {
		if (instances == null || weights == null)
			return instances;

		if (instances.size() != weights.size())
			throw new IllegalArgumentException();

		for (int i = 0; i < weights.size(); i++) {
			double w = weights.get(i);
			if (Double.isNaN(w))
				w = 0;
			instances.instance(i).setWeight(w);
		}

		return instances;

	}

	public static Instances getLabeledInstances(List<NumericalFeatureVector> fvs, List<String> labels) {
		Instances insances = getInstances(fvs, false);

		return addLabelsToInstances(insances, labels);
	}

	/**
	 * what is this good for?!
	 * 
	 * @param mfvs
	 * @param numLabels
	 * @param stringToNominal
	 * @return
	 */
	public static Instances getNumericLabeledMixInstances(List<MixedDataFeatureVector> mfvs, List<Double> numLabels, boolean stringToNominal) {
		Instances insances = getInstances(mfvs, stringToNominal);

		return addNumericLabelsToInstances(insances, numLabels);
	}

	/**
	 * what is this good for?!
	 * 
	 * @param insances
	 * @param numLabels
	 * @return
	 */
	private static Instances addNumericLabelsToInstances(Instances insances, List<Double> numLabels) {
		Attribute classAtt = new Attribute("num");

		insances.insertAttributeAt(classAtt, insances.numAttributes());
		insances.setClassIndex(insances.numAttributes() - 1);

		for (int i = 0; i < numLabels.size(); i++)
			insances.instance(i).setValue(insances.numAttributes() - 1, numLabels.get(i));

		return insances;

	}

	public static Instances addLabelAttributeToInstance(Instances instances, List<String> labels) {
		List<String> distinctLabels = distinctListCreator(labels);

		Attribute classAtt = new Attribute("class", distinctLabels);

		instances.insertAttributeAt(classAtt, instances.numAttributes());
		instances.setClass(classAtt);
		instances.setClassIndex(instances.numAttributes() - 1);

		return instances;
	}

	public static Instances addNumericLabelAttributeToInstance(Instances insances) {
		Attribute classAtt = new Attribute("num");

		insances.insertAttributeAt(classAtt, insances.numAttributes());
		insances.setClass(classAtt);
		insances.setClassIndex(insances.numAttributes() - 1);

		return insances;

	}

	private static Instances addLabelsToInstances(Instances instances, List<String> labels) {
		if (instances == null)
			return null;

		Instances inst2 = addLabelAttributeToInstance(instances, labels);

		for (int i = 0; i < labels.size(); i++)
			inst2.instance(i).setClassValue(labels.get(i));

		return inst2;
	}

	/**
	 * Improve this piece of code!
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> distinctListCreator(List<String> list) {
		List<String> distinctList = new ArrayList<String>();

		if (list == null)
			return distinctList;

		for (String str : list)
			if (!distinctList.contains(str))
				distinctList.add(str);

		return distinctList;
	}

	private static <O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> void fillInstanceByIndex(Instance instance, FV fv, int targetLength) {
		if (fv == null)
			return;

		if (fv.getDimensions() != targetLength)
			throw new IllegalArgumentException("WekaConversion: length of given featurevector does not match target instance feature length");

		List<? extends Feature<O>> features = fv.getVectorRepresentation();

		for (int i = 0; i < features.size(); i++) {
			if (features.get(i).getFeatureType() == FeatureType.DOUBLE)
				instance.setValue(i, (Double) features.get(i).getFeatureValue());
			else if (features.get(i).getFeatureType() == FeatureType.STRING) {
				Object o = features.get(i).getFeatureValue();
				if (o == null)
					instance.setValue(i, '?');
				else
					instance.setValue(i, o.toString());
			} else if (features.get(i).getFeatureType() == FeatureType.BOOLEAN) {
				Boolean b = (Boolean) features.get(i).getFeatureValue();
				if (b == null)
					throw new IllegalArgumentException("WekaConversion: boolean feature was null. cannot be assigned to an instance.");

				Integer integer = b.booleanValue() ? 1 : 0;
				instance.setValue(i, ((Number) integer).doubleValue());
			} else
				throw new IllegalArgumentException("WekaConversion: unsupported feature type");
		}
	}
}
