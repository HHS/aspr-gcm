package gcm.manual;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import gcm.util.TimeElapser;

public class MT_Optional {

	private static class Dude {
		private Map<Integer, String> map = new LinkedHashMap<>();

		public String getValue(int index) {
			return map.get(index);
		}

		public Dude(Map<Integer, String> map) {
			this.map.putAll(map);
		}
	}

	private static class OptionalDude {
		private Map<Integer, String> map = new LinkedHashMap<>();

		public Optional<String> getValue(int index) {
			return Optional.ofNullable(map.get(index));
		}

		public OptionalDude(Map<Integer, String> map) {
			this.map.putAll(map);
		}
	}
	
	private static String getRandomizedString(Random random) {
		int len = random.nextInt(12)+1;
		
		StringBuilder sb = new StringBuilder();
		for(int i= 0;i<len;i++) {
		  int k = (random.nextInt(26)+97);
		  char c = (char)k;
		  sb.append(c);
		}
		
		return sb.toString();
	}

	public static void main(String[] args) {
		Random random = new Random();
		Map<Integer, String> map = new LinkedHashMap<>();
		int n = 1000000;
		for (int i = 0; i < n; i++) {
			map.put(random.nextInt(n * 5), getRandomizedString(random));
		}
		
		Dude dude = new Dude(map);
		OptionalDude optionalDude = new OptionalDude(map);
		
		TimeElapser timeElapser = new TimeElapser();
		
		int dudeNullCount = 0;
		for (int i = 0; i < n; i++) {
			int index = random.nextInt(n * 5);
			String value = dude.getValue(index);
			if(value == null) {
				dudeNullCount++;
			}
		}
		System.out.println(timeElapser.getElapsedMilliSeconds()+" "+dudeNullCount);
		timeElapser.reset();
		int optionalDudeNullCount = 0;
		for (int i = 0; i < n; i++) {
			int index = random.nextInt(n * 5);
			String value = optionalDude.getValue(index).orElse(null);
			if(value == null) {
				optionalDudeNullCount++;
			}
		}
		System.out.println(timeElapser.getElapsedMilliSeconds()+" "+optionalDudeNullCount);

	}
}
