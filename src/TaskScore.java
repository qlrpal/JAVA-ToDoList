public class TaskScore {
    private int score;
    //score는 0 밑으로는 안떨어지도록 만들었습니다.
    //아직 본 코드에 적용은 안했습니다

    public int getScore() {
        return score;
    }

    public void addScore(int score)   {
        setScore(this.score + score);
    }

    public void setScore(int score) {
        if(score >= 0)  {
            this.score = score;
        }
        else    {
            this.score = 0;
        }
    }
}
