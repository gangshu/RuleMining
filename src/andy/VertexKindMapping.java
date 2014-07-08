package andy;


public class VertexKindMapping {
  
    static public String getVertex_kind(int id) {
        switch (id) {
            case 1: return "AI";
            case 2: return "AO";
            case 3: return "AU";
            case 4: return "BD";
            case 5: return "CS";
            case 6: return "CP";
            case 7: return "DL";
            case 8: return "EY";
            case 9: return "ET";
            case 10: return "EX";
            case 11: return "FI";
            case 12: return "FO";
            case 13: return "GAI";
            case 14: return "GAO";
            case 15: return "GFI";
            case 16: return "GFO";
            case 17: return "IC";
            case 18: return "JP";
            case 19: return "LB";
            case 20: return "RT";
            case 21: return "SW";
            case 22: return "VI";
        }
        return "NB ";
    }
}
