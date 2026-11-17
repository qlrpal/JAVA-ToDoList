public class TaskScore {
    private int score;
    private int treeLevel;
    Ascii ascii = new Ascii();

    //score는 0 밑으로는 안떨어지도록 만들었습니다.
    //아직 본 코드에 적용은 안했습니다

    public TaskScore(int score) {
        this.score = score;
    }

    public void treeGrow(int score){
        if (score <= 10){
            treeLevel = 1;
            System.out.println(ascii.getOneStepTree());
            System.out.println("포인트 : " + score);
            System.out.println("당신의 레벨은 씨앗입니다!");
        }else if ((score > 10)&&(score <= 20)){
            treeLevel = 2;
            System.out.println(ascii.getTwoStepTree());
            System.out.println("포인트 : " + score);
            System.out.println("당신의 레벨은 새싹입니다!");
        }else if ((score > 20)&&(score <= 30)){
            treeLevel = 3;
            System.out.println(ascii.getThreeStepTree());
            System.out.println("포인트 : " + score);
            System.out.println("당신의 레벨은 묘목입니다!");
        }else if ((score > 30)&&(score <= 40)){
            treeLevel = 4;
            System.out.println(ascii.getFourStepTree());
            System.out.println("포인트 : " + score);
            System.out.println("당신의 레벨은 나무입니다!");
        }else{
            treeLevel = 5;
            System.out.println(ascii.getFiveStepTree());
            System.out.println("포인트 : " + score);
            System.out.println("당신의 레벨은 세계수입니다!");
        }



    }


    public int getTreeLevel() {
        return treeLevel;
    }

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
