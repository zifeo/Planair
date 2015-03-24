abstract class Scorable extends Accelerable {

  private final ArrayList<Scorer> scoreObservers;
  
  Scorable(PVector location) {
    super(location);
    this.scoreObservers = new ArrayList<Scorer>();
  }
    
  public void addScoreObserver(Scorer scorer) {
    scoreObservers.add(scorer);
  }
  
  protected void notifyScore(int delta) {
    for (Scorer scorer : scoreObservers) {
      scorer.notifiedScore(delta);
    }
  } 
  
}
