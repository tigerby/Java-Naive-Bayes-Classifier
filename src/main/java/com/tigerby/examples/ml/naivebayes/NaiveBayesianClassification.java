package com.tigerby.examples.ml.naivebayes;

/**
 * @author <a href="mailto:bongyeonkim@coupang.com">lion</a>
 * @since 2015.10.23
 */
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NaiveBayesianClassification {

	private String[] dataSet;
	private Map<String, Long> classifies = new HashMap<>();
	private Map<String, Map<String, Long>> counter = new HashMap<>();

	public NaiveBayesianClassification(String[] dataSet) {
		if (dataSet == null || dataSet.length == 0)
			throw new IllegalArgumentException("Empty dataSet");
		this.dataSet = dataSet;
	}

	private String getClassify(String input) {
		int divide = input.indexOf('|');
		return divide > - 1 ? input.substring(0, divide) : null;
	}

	private String[] getWords(String input) {
		int divide = input.indexOf('|');
		return divide > -1 ? input.substring(divide+1).split(",") : null;
	}

	public void training() {
		Arrays.stream(dataSet).forEach(data -> {
			String classify = getClassify(data);
			String[] words = getWords(data);
			//-- 분류명과 분류명이 나타난 횟수를 classifies에 저장한다.
			if (classify != null) {
				Long count = classifies.get(classify);
				if (count == null)
					count = 1L;
				else
					count++;
				classifies.put(classify, count);
				//-- 각 분류명에 대해 특정 단어가 나타난 횟수를 couner에 저장한다.
				if (words != null) {
					Arrays.stream(words).forEach(word -> {
						Map<String, Long> wordCounter = counter.get(classify);
						if (wordCounter == null) {
							wordCounter = new HashMap<>();
							counter.put(classify, wordCounter);
						}
						Long wordCount = wordCounter.get(word);
						if (wordCount == null)
							wordCount = 1L;
						else
							wordCount++;
						wordCounter.put(word, wordCount);
					});
				}
			}
		});
	}

	public String judgment(String[] words) {
		Map<String, Double> results = new HashMap<>();
		long classifiesTotalCount = classifies.values().stream().mapToLong(Long::longValue).sum();
		classifies.forEach((classify, count) -> {
			double[] points = Arrays.stream(words).mapToDouble(word -> {
				Map<String, Long> wordCounter = counter.get(classify);
				if (wordCounter == null)
					return 0.0f;
				Long wordCount = wordCounter.get(word);
				if (wordCount == null)
					return 0.0f;
				long wordTotalCount = wordCounter.values().stream().mapToLong(Long::longValue).sum();
				return (double)wordCount / wordTotalCount;
			}).toArray();
			double total = (double)classifies.get(classify) / classifiesTotalCount;
			total = Arrays.stream(points).reduce(total, (x, y) -> x * y);
			results.put(classify, total);
		});
		results.entrySet().forEach(entry ->
			System.out.println(String.format("%s : %f", entry.getKey(), entry.getValue())));
		return results.entrySet().stream().max(Map.Entry.comparingByValue(Double::compareTo)).get().getKey();
	}

	public static void main(String[] args) throws Exception {
		//-- 학습 데이터
		String[] dataSet = {
			"Comedy|fun,couple,love,love",
			"Action|fast,furious,shoot",
			"Comedy|couple,fly,fast,fun,fun",
			"Action|furious,shoot,shoot,fun",
			"Action|fly,fast,shoot,love"
		};
		//-- 테스트 데이터
		String[] words = {"fun", "furious", "fast"};

		NaiveBayesianClassification classifier = new NaiveBayesianClassification(dataSet);
		classifier.training();
		String classify = classifier.judgment(words);
		System.out.println(classify);
	}

}