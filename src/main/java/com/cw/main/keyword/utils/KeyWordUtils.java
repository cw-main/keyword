package com.cw.main.keyword.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.*;
import java.util.*;

/**
 * 匹配关键字
 */
@Slf4j
public class KeyWordUtils {


    /** 目录读取敏感词 */
    private static final String FILE_PATH = "classpath*:/sensitivewordlibrary/*";
    /** 系统自定义铭感词文件形式 */
    public static Map<String, Object> KEY_WORD_MAP_FILE = new HashMap<>();

    /** 特殊字符文件名 */
    private static final String SPECIAL_CHAR = "sensitivewordlibrary/special_char.txt";
    /** 特殊字符 */
    public static Set<String> SPECIAL_SET = new HashSet<>();

    /** 忽略的文件 */
    private static final String TEXT_TXT = "README.md";


    /** 用户自定义扩展铭感词(如在自己库里存的) */
    public static Map<String, Object> KEY_WORD_MAP_CUSTOM = new HashMap<>();

    /** 最小匹配 */
    public static Integer MIN_MATCH_TYPE = 1;

    /** 最大匹配 */
    public static Integer MAX_MATCH_TYPE = 2;

    /** 最大匹配 */
    public static char DEFAULT_REPLACE_CHAR = '*';

    static {
        log.info("KeyWordUtils 初始化文件关键字 begin =====================================");
        getKeyWordMapByFile();
        log.info("KeyWordUtils 初始化文件关键字 end size:{} =====================================", KEY_WORD_MAP_FILE.size());
    }

    /**
     * 在 keyWordSet 匹配关键字
     *
     * @param sourceStr 需要匹配的文本
     * @return 包含的关键字
     */
    public static String checkSensitiveWord(String sourceStr) {
        return checkSensitiveWordForSysAndKeyWordSet(null, sourceStr, null, null);
    }

    /**
     * 匹配关键字，不去除特殊字符
     *
     * @param keyWordSet 要匹配的关键字  有值，则匹配系统自带和传入的关键字;
     *                   同一程序多次匹配 只需要第一次传入就行,后面传入空 会复用之前的
     * @param sourceStr  文本
     * @param matchType  匹配规则 1:最小匹配规则，2:最大匹配规则 默认最大  如:1 如果关键字包括(中国、中国人) 则 会匹配 中国 返回
     * @return 包含的关键字
     */
    public static String checkSensitiveWordIncludSpecialChar(Set<String> keyWordSet, String sourceStr, Integer matchType) {
        log.info("KeyWordUtils.checkSensitiveWord begin keyWordSet.size:{};", keyWordSet == null ? 0 : keyWordSet.size());
        return checkSensitiveWordForSysAndKeyWordSet(keyWordSet, sourceStr, matchType, Boolean.FALSE);

    }

    /**
     * 在 keyWordSet 匹配关键字
     *
     * @param keyWordSet                    要匹配的关键字  多次匹配 只需要第一次传入就行;后面传入空 会复用之前的
     * @param sourceStr                     需要匹配的文本
     * @param matchType                     匹配规则 1:最小匹配规则，2:最大匹配规则 默认最大;  如:1 如果关键字包括(中国、中国人) 则 会匹配 中国 返回
     * @param compareNotContainsSpecialChar 是否不包含特殊字符比较 即（sensitivewordlibrary/special_char.txt）
     * @return 包含的关键字
     */
    public static String checkSensitiveWord(Set<String> keyWordSet, String sourceStr, Integer matchType, Boolean compareNotContainsSpecialChar) {
        return checkSensitiveWordForSysAndKeyWordSet(keyWordSet, sourceStr, matchType, compareNotContainsSpecialChar);
    }


    /**
     * 匹配系统关键字
     *
     * @param sourceStr
     * @param matchType
     * @return
     */
    private static String checkSensitiveWordForSys(String sourceStr, Integer matchType) {
        return KeyWordUtils.checkSensitiveWord(sourceStr, matchType, KEY_WORD_MAP_FILE);
    }

    /**
     * 匹配用户自定义关键字
     *
     * @param sourceStr
     * @param matchType
     * @return
     */
    private static String checkSensitiveWordForCustom(String sourceStr, Integer matchType) {
        return KeyWordUtils.checkSensitiveWord(sourceStr, matchType, KEY_WORD_MAP_CUSTOM);
    }


    private static String checkSensitiveWordForSysAndKeyWordSet(Set<String> keyWordSet, String sourceStr, Integer matchType, Boolean compareNotContainsSpecialChar) {

        if (StringUtils.isEmpty(sourceStr)) {
            return sourceStr;
        }

        //默认最小匹配
        if (matchType == null) {
            matchType = MIN_MATCH_TYPE;
        }

        //是否去除特殊字符
        boolean isNotContainsSpecialChar = compareNotContainsSpecialChar != null && compareNotContainsSpecialChar;
        String copySourceStr = isNotContainsSpecialChar ? getStrNotContainsSpecialChar(sourceStr) : sourceStr;

        //初始化用户自定义关键词
        if (keyWordSet != null && keyWordSet.size() > 0) {
            KEY_WORD_MAP_CUSTOM = KeyWordUtils.addKeyWordToHashMap(keyWordSet);
        }

        //系统自定义
        String result = KeyWordUtils.checkSensitiveWordForSys(copySourceStr, matchType);
        //用户自定义
        if (keyWordSet != null && keyWordSet.size() > 0) {
            result += KeyWordUtils.checkSensitiveWordForCustom(copySourceStr, matchType);
        }

        return result;
    }

    /**
     * 将关键字集合 转为DFA模型
     * isEnd 0:不是关键字结束; 1:关键字结束;
     *
     * @param keyWordSet 关键字集合
     * @return
     */
    private static Map<String, Object> addKeyWordToHashMap(Set<String> keyWordSet) {
        Map<String, Object> resultMap = new HashMap<>(keyWordSet.size());
        String key = null;
        Map nowMap = null;
        Map<String, String> newWorMap = null;
        Iterator<String> iterator = keyWordSet.iterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            nowMap = resultMap;
            for (int i = 0; i < key.length(); i++) {
                char keyChar = key.charAt(i);
                Object wordMap = nowMap.get(keyChar);

                if (wordMap != null) {
                    nowMap = (Map) wordMap;
                } else {
                    newWorMap = new HashMap<>();
                    newWorMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }

                //结束标志 1
                if (i == key.length() - 1) {
                    nowMap.put("isEnd", "1");
                }
            }
        }
        return resultMap;
    }


    /**
     * 替换系统为 *
     *
     * @param sourceStr 要匹配的关键字
     * @return 返回替换后的文本
     */
    public static String replaceKeyWordForSys(String sourceStr) {
        return replaceKeyWordForSourceMap(sourceStr, DEFAULT_REPLACE_CHAR, MIN_MATCH_TYPE, KEY_WORD_MAP_FILE);
    }


    /**
     * 替换用户自定义
     *
     * @param sourceStr 要匹配的关键字
     * @param target    要替换的字符 如要将 你好替换成** 传入 * 即可
     * @param matchType 匹配规则 1:最小匹配规则，2:最大匹配规则 默认最大
     *                  如:1 如果关键字包括(中国、中国人) 则 会匹配 中国 返回
     * @return 返回替换后的文本
     */
    public static String replaceKeyWordForCustom(Set<String> keyWordSet, String sourceStr, Integer matchType, Character target) {


        String result = KeyWordUtils.replaceKeyWordForSourceMap(sourceStr, target, matchType, KEY_WORD_MAP_FILE);
        if (keyWordSet != null && keyWordSet.size() > 0) {
            result += KeyWordUtils.replaceKeyWordForSourceMap(sourceStr, target, matchType, KEY_WORD_MAP_CUSTOM);
        }

        return result;
    }

    /**
     * 替换sourceStr中在 从sourceMap匹配到的关键字
     *
     * @param sourceStr 要匹配的关键字
     * @param target    要替换的字符 如要将 你好替换成** 传入 * 即可
     * @return 返回替换后的文本
     */
    public static String replaceKeyWordForSourceMap(String sourceStr, Character target, Integer matchType, Map<String, Object> sourceMap) {

        if (StringUtils.isEmpty(sourceStr)) {
            return sourceStr;
        }

        StringBuilder sb = new StringBuilder();
        if (target == null) {
            target = '*';
        }

        for (int i = 0; i <= 10; i++) {
            sb.append(target);
        }
        String targetStr = sb.toString();
        if (matchType == null) {
            matchType = 1;
        }

        String result = sourceStr;
        //双层循环 外层循环控制开始数,内层循环控制匹配一个关键字后重新开始的数,从头匹配关键字
        for (int i = 0; i < result.length(); i++) {
            int endIndex = KeyWordUtils.getKeyWordEndIndex(result, i, matchType, sourceMap);
            //获取匹配到的关键字
            if (endIndex > 0) {
                String substring = result.substring(i, endIndex + 1);
                int count = endIndex - i + 1;
                if (count <= 10) {
                    result = result.replace(substring, targetStr.substring(0, count));
                } else {
                    result = replaceCycle(substring, count, target);
                }
            }
        }
        return result;
    }

    private static String replaceCycle(String result, int count, Character target) {
        StringBuffer sb = new StringBuffer(count);
        for (int i = 0; i < count; i++) {
            sb.append(target);
        }
        return result.replace(result, sb.toString());
    }


    /**
     * 匹配一次指定来源(sourceMap)中的关键字
     *
     * @param sourceStr 要匹配的关键字
     * @param matchType 匹配规则 1:最小匹配规则，2:最大匹配规则 默认最大
     *                  如:1 如果关键字包括(中国、中国人) 则 会匹配 中国 返回
     * @param sourceMap 需要比对的map
     * @return 匹配到的所有的关键字
     */
    private static String checkSensitiveWord(String sourceStr, Integer matchType, Map<String, Object> sourceMap) {

        if (sourceMap == null || sourceMap.size() == 0) {
            log.warn("KeyWordUtils.checkSensitiveWord sourceMap is Empty");
            return StringUtils.EMPTY;
        }

        StringBuilder result = new StringBuilder();
        Set<String> existKeyWord = new HashSet<>();
        //双层循环 外层循环控制开始数,内层循环控制匹配一个关键字后重新开始的数,从头匹配关键字
        for (int i = 0; i < sourceStr.length(); i++) {
            int endIndex = KeyWordUtils.getKeyWordEndIndex(sourceStr, i, matchType, sourceMap);
            //获取匹配到的关键字
            if (endIndex > 0) {
                String substring = sourceStr.substring(i, endIndex + 1);
                existKeyWord.add(substring);
            }
        }

        existKeyWord.forEach(item -> {
            result.append(item).append(" ");
        });
        return result.toString();
    }

    /**
     * 从指点下标开始比,返回匹配到的关键字下标,没有匹配到返回 0
     *
     * @param sourceStr
     * @param beginIndex 开始的下标
     * @param matchType  匹配规则 1:最小匹配规则，2:最大匹配规则 默认最大
     *                   如:1 如果关键字包括(中国、中国人) 则 会匹配 中国 返回
     * @param targetMap  需要比对的map
     * @return 匹配到的单个关键字的下标
     */
    public static int getKeyWordEndIndex(String sourceStr, int beginIndex, Integer matchType, Map<String, Object> targetMap) {
        int count = 0;
        int endIndex = 0;
        Map<String, Object> tempMap = targetMap;
        for (int j = beginIndex; j < sourceStr.length(); j++) {
            char temp = sourceStr.charAt(j);
            tempMap = (Map) tempMap.get(temp);
            if (tempMap == null) {
                //没有匹配到直接跳出
                break;
            }
            //匹配到了关键字
            count++;
            Object isEnd = tempMap.get("isEnd");
            if ("1".equals(String.valueOf(isEnd))) {
                //关键字结束
                if (count >= 2) {
                    endIndex = j;
                    if ("1".equals(String.valueOf(matchType))) {
                        break;
                    }
                }
            }
        }
        return endIndex;
    }

    /**
     * 读取文件中的关键字放入成员变量 KEY_WORD_MAP_FILE
     */
    public static void getKeyWordMapByFile() {

        Map<String, Object> stringObjectMap = KeyWordUtils.addKeyWordToHashMap(getKeyWordMapByFileToSet());
        KEY_WORD_MAP_FILE.putAll(stringObjectMap);

    }

    /**
     * 读取文件关键字 返回Set集合
     *
     * @return
     */
    public static Set<String> getKeyWordMapByFileToSet() {

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Set<String> keyWords = new HashSet<>();
        BufferedReader bufferedReader = null;
        try {
            Resource[] resources = resolver.getResources(FILE_PATH);
            if (resources == null) {
                return keyWords;
            }
            for (Resource tempResource : resources) {
                File file = tempResource.getFile();
                if (file == null) {
                    continue;
                }
                if (TEXT_TXT.equals(file.getName()) || SPECIAL_CHAR.equals(file.getName())) {
                    //测试文件不计入
                    continue;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                Set<String> eachFile = KeyWordUtils.readFile(bufferedReader, false);
                if (eachFile != null && eachFile.size() > 0) {
                    keyWords.addAll(eachFile);
                }
            }
            return keyWords;
        } catch (Exception e) {
            log.error("KeyWordUtils.getKeyWordMapByFile error:", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error("KeyWordUtils.getKeyWordMapByFile 关闭流 error:", e);
                }
            }
        }
        return keyWords;
    }

    /**
     * @return 返回 sourceStr 去除特殊字符
     * 建议文件只存单个特殊字符，一个字符一行，因为存多个可能存在情况：
     * 文件:  -  ()
     * 源字符串: A(-)B
     * 取出后可能为空,而实际上 (-)和AB是合法的
     */
    public static String getStrNotContainsSpecialChar(String sourceStr) {

        if (StringUtils.isEmpty(sourceStr)) {
            return sourceStr;
        }

        //初始化特殊字符集合
        if (SPECIAL_SET.size() == 0) {
            log.error("KeyWordUtils.getStrNotContainsSpecialChar begin====");
            InputStream resourceAsStream = KeyWordUtils.class.getClassLoader().getResourceAsStream(SPECIAL_CHAR);
            if (resourceAsStream == null) {
                return sourceStr;
            }

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
                Set<String> specialCharList = KeyWordUtils.readFile(bufferedReader, true);
                SPECIAL_SET = specialCharList;
            } catch (Exception e) {
                log.error("KeyWordUtils.getStrNotContainsSpecialChar error:", e);
                return sourceStr;
            }
        }

        log.error("KeyWordUtils.getStrNotContainsSpecialChar end====");
        for (String item : SPECIAL_SET) {
            sourceStr = StringUtils.remove(sourceStr, item);
        }

        return sourceStr;

    }

    private static Set<String> readFile(BufferedReader bufferedReader, Boolean isChar) throws Exception {
        Set<String> result = new HashSet<>();
        if (bufferedReader == null) {
            return result;
        }

        String str;
        while ((str = bufferedReader.readLine()) != null) {
            if (isChar) {
                if (str.length() > 1) {
                    //如果是字符长度大于一则忽略
                    continue;
                }
            }
            String eachKeyWord = StringUtils.trimToEmpty(str);
            if (StringUtils.isNotEmpty(eachKeyWord)) {
                String[] split = eachKeyWord.split("\\|");
//                if (eachKeyWord.length() > CircleEnum.EnumOfNum.KEY_WORD_MAX_LENGTH.getCode()) {
//                    log.error("关键词 {} 长度大于{}", eachKeyWord, CircleEnum.EnumOfNum.KEY_WORD_MAX_LENGTH.getCode());
//                    continue;
//                }
                List<String> strings = Arrays.asList(split);
                result.addAll(strings);
            }
        }
        if (isChar) {
            //空格
            result.add(" ");
        }
        return result;
    }

    /**
     * 读取文件中的关键字,生成insert语句 返回的是最大的关键字的长度
     */

    public static Integer readFileAndGenerateInsert(Set<String> targetSet) {
        Integer maxLength = 0;
        Set<String> keyWordMapByFileToSet = (targetSet == null || targetSet.size() == 0) ? getKeyWordMapByFileToSet() : targetSet;
        if (keyWordMapByFileToSet == null || keyWordMapByFileToSet.size() == 0) {
            return maxLength;
        }

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("sql.sql"));
            StringBuffer sb = new StringBuffer();
            for (String temp : keyWordMapByFileToSet) {
                if (StringUtils.isEmpty(temp)) {
                    continue;
                }
                maxLength = Math.max(maxLength, temp.length());
                sb.append("INSERT INTO `keyword` (`keyword`,`sys_keep`) VALUES ('").append(temp).append("',").append("0").append(")").append(";").append("\r\n");
            }
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxLength;
    }

    public static void main(String[] args) {
        Integer integer = readFileAndGenerateInsert(null);
        System.out.println(integer);
    }

}
