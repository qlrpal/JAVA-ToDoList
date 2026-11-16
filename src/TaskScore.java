public class TaskScore {
    private int score;
    //score는 0 밑으로는 안떨어지도록 만들었습니다.
    //아직 본 코드에 적용은 안했습니다

    public int getScore() {
        return score;
    }

    public void threeAddScore(int score) {
        this.score = score+3;
    }
    public void oneAddScore(int score) {
        this.score = score+1;
    }
    public void threeMinusScore(int score) {
        this.score = score-3;
        if (this.score<0){
            this.score=0;
        }
    }
    public void oneMinusScore(int score) {
        this.score = score-1;
        if (this.score<0){
            this.score=0;
        }
    }

}
