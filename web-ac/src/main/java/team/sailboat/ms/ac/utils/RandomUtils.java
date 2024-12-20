package team.sailboat.ms.ac.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RandomUtils {
	public static String generateMixed(int n) {
		char[] chars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
				'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' , 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
				's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
		StringBuffer res = new StringBuffer("");
		for (int i = 0; i < n; i++) {
			int id = (int) Math.round(Math.random() * 61);
			res.append(chars[id]);
		}
		return res.toString();
	};
	/**
	 * 计算多久会出现重复随机数字
	 * @param mixed 随机位数
	 * @param caculateTimes 统计次数
	 */
	public static void caculateRepeatTime(int mixed,int caculateTimes){
		List<Integer> maxList = new ArrayList<Integer>();
		for(int m = 0;m<caculateTimes;m++){
			Set<String> set = new HashSet<String>();
			int i = 1;
			while(true){
				String rdm = RandomUtils.generateMixed(mixed);
				if(set.contains(rdm))
					break;
				set.add(rdm);
				i++;
			}
			maxList.add(i);
		}
		Collections.sort(maxList);
		for (Integer integer : maxList) {
			System.out.println(integer);
		}
	}
	public static void main(String[] args){
//		caculateRepeatTime(5, 100);
	}
}
