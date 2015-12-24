public class Sample1 {
    public static void main(String[] args) {
        def res = []
        def cin = new Scanner(System.in)
        while(cin.hasNextInt()) res.add cin.nextInt() + cin.nextInt()
        for(item in res) println item
    }
}
