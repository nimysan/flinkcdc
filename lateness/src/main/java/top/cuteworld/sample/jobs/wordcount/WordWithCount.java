package top.cuteworld.sample.jobs.wordcount; /* 记录单词及其出现频率的Pojo
 */

public class WordWithCount {
    /**
     * 单词内容
     */
    public String word;

    /**
     * 出现频率
     */
    public long count;

    public WordWithCount() {
        super();
    }

    public WordWithCount(String word, long count) {
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    /**
     * 将单词内容和频率展示出来
     *
     * @return
     */
    @Override
    public String toString() {
        return word + " : " + count;
    }
}