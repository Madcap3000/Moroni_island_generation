import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.Scanner;


public class voronoi_main extends JFrame {

    static final double step = 0.5;

    static final boolean RandomMap = false;
    static voronoi_main map;

    static BufferedImage I;
    static BufferedImage J;
    static int px[], py[], color[], cells = 170, size = 1600, compressed_size = 800;
    static int currentlyOccupied = 0;
    static Line2D.Double[][] allLines;

    public voronoi_main(){

        super("Voronoi Diagram");
            setBounds(0, 0, compressed_size, compressed_size);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            Random rand = new Random();
            //rand.setSeed(1000362);
            I = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            px = new int[cells];
            py = new int[cells];
            color = new int[cells];
            allLines = new Line2D.Double[cells][cells];

            if(RandomMap) {
                for (int i = 0; i < cells; i++) {
                    px[i] = rand.nextInt(size - 100) + 50;
                    py[i] = rand.nextInt(size - 100) + 50;
                }
            }else{
                readFromFile("./src/dataPoints");
                cells = currentlyOccupied;
            }

        for (int i = 0; i < cells; i++) {
            int new_color = 255;
            new_color = new_color * 256 + rand.nextInt(255);
            new_color = new_color * 256 + rand.nextInt(255);
            new_color = new_color * 256 + rand.nextInt(255);
            color[i] = new_color;
        }


            //calculate all lines
            for (int a = 0; a < cells; a++) {
                for (int b = 0; b < cells; b++) {
                    allLines[a][b] = null;
                }
            }
            for (int a = 0; a < cells; a++) {
                for (int b = a + 1; b < cells; b++) {
                    allLines[a][b] = bisector(new Point2D.Double(px[a], py[a]), new Point2D.Double(px[b], py[b]));
                }
            }

            Graphics2D g = I.createGraphics();

            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, size, size);
            g.setComposite(AlphaComposite.Src);

        /*g.setColor(Color.LIGHT_GRAY);
        for (int a = 0; a < cells; a++) {
            for (int b = a + 1; b < cells; b++) {
                g.drawLine((int) allLines[a][b].x1 ,(int) allLines[a][b].y1 ,(int) allLines[a][b].x2 ,(int) allLines[a][b].y2);
            }
        }*/

        drawLines(g, new Color(150, 150, 150), 36);
        drawLines(g,new Color(60,60,60),33);
        drawLines(g,new Color(60,60,60),30);
        drawLines(g,new Color(60,60,60),27);
        drawLines(g,new Color(60,60,60),33);

        drawLines(g,new Color(150,150,150),18);

        /*g.setColor(Color.GREEN);
        for (int a = 0; a < cells; a++) {
            for (int b = a + 1; b < cells; b++) {
                for (int c = b + 1; c < cells; c++) {
                    Point2D.Double p = calculateIntersectionPoint(allLines[a][b],allLines[b][c]);
                    g.fill(new Ellipse2D.Double(p.x - 2.5, p.y - 2.5, 5, 5));
                }
            }
        }*/

        g.setColor(Color.BLUE);
        for (int i = 0; i < cells; i++) {
            g.fill(new Ellipse2D.Double(px[i] - 2.5, py[i] - 2.5, 30, 30));
        }
            try {
                J = simpleResizeImage(I, compressed_size, compressed_size);
                ImageIO.write(I, "png", new File("voronoi(2).png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
    }



    static Line2D.Double bisector(Point2D.Double p1, Point.Double  p2){
        if(p2.x == p1.x){
            return new Line2D.Double(new Point2D.Double(0, (p2.y + p1.y) / 2.0),new Point2D.Double(size, (p2.y + p1.y) / 2.0));
        }else if(p2.y == p1.y) {
            return new Line2D.Double(new Point2D.Double ((p2.x + p1.x) / 2.0, 0),new Point2D.Double((p2.x + p1.x) / 2.0, 0));
        }else {
            Point.Double midpoint = new Point.Double((p1.x + p2.x) / 2.0, (p1.y + p2.y) / 2.0);
            double slope = -(1.0 / ((p2.y - p1.y) / (p2.x - p1.x)));
            //y = slope * x + b
            double b = midpoint.y - midpoint.x * slope;
            if(1 > slope && slope > -1) {
                return new Line2D.Double(new Point2D.Double(0, b), new Point2D.Double(size, size * slope + b));
            }else{
                return new Line2D.Double(new Point2D.Double((0 - b) / slope , 0), new Point2D.Double((size - b) / slope, size));
            }
        }
    }

    public static Point2D.Double calculateIntersectionPoint(Line2D.Double line1, Line2D.Double line2){
        double D = (line1.x1 - line1.x2) * (line2.y1 - line2.y2) - (line1.y1 - line1.y2) * (line2.x1 - line2.x2);
        if(D != 0.0) {
            double px = (line1.x1 * line1.y2 - line1.y1 * line1.x2) * (line2.x1 - line2.x2) -
                    (line1.x1 - line1.x2) * (line2.x1 * line2.y2 - line2.y1 * line2.x2);
            double py = (line1.x1 * line1.y2 - line1.y1 * line1.x2) * (line2.y1 - line2.y2) -
                    (line1.y1 - line1.y2) * (line2.x1 * line2.y2 - line2.y1 * line2.x2);
            return new Point2D.Double(px / D, py / D);
        }else{
            return null;
        }
    }

    public static Point2D.Double nextPointOnLine(Point2D.Double startPoint, int line_a, int line_b, boolean dir){
        double local_step = (dir ? step:-step);
        if(allLines[line_a][line_b].x1 == allLines[line_a][line_b].x2){
            return new Point2D.Double(startPoint.x, startPoint.y + local_step);
        }else if(allLines[line_a][line_b].y1 == allLines[line_a][line_b].y2){
            return new Point2D.Double(startPoint.x + local_step, startPoint.y);
        }else{
            double slope = (allLines[line_a][line_b].y1 - allLines[line_a][line_b].y2) / (allLines[line_a][line_b].x1 - allLines[line_a][line_b].x2);
            if(1 > slope && slope > -1){
                return new Point2D.Double(startPoint.x + local_step, startPoint.y + local_step * slope);
            }else{
                return new Point2D.Double(startPoint.x + local_step / slope, startPoint.y + local_step);
            }
        }
    }

    public static void drawLines(Graphics2D g, Color color, double radius){
        g.setColor(color);
        for (int a = 0; a < cells; a++) {
            for (int b = a + 1; b < cells; b++) {
                Point2D.Double currentPoint = (Point2D.Double) allLines[a][b].getP1();
                for (int c = 0; c < size / step; c++) {
                    currentPoint = nextPointOnLine(currentPoint,a,b,true);
                    boolean paint = true;
                    double lengthToA = distance(px[a], currentPoint.x, py[a], currentPoint.y);
                    for(int i = 0; i < cells; i ++){
                        if(i != a && i != b && (lengthToA > distance(currentPoint.x, px[i], currentPoint.y, py[i]))){
                            paint = false;
                            break;
                        }
                    }
                    if(paint) {
                        g.fill(new Ellipse2D.Double(currentPoint.x - radius, currentPoint.y - radius, 2 * radius, 2 * radius));
                    }
                }
            }
        }
    }

    public int getColor(int color, int alpha){
        return color % 16777216 + alpha * 16777216;
    }

    public void paint(Graphics g) {
        g.drawImage(J, 0, 0, this);
    }

    static double distance(double x1, double x2, double y1, double y2) {
        double d;
        d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return d;
    }

    BufferedImage simpleResizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        return Scalr.resize(originalImage, targetWidth, targetHeight);
    }

    public static void readFromFile(String name) {
        currentlyOccupied = 0;
        Scanner scanner;
        try {
            scanner = new Scanner(new File(name));
        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage());
            return;
        }

        while(scanner.hasNext()){
            String[] tokens = scanner.nextLine().split(" ");
            px[currentlyOccupied] = Integer.parseInt(tokens[0]);
            py[currentlyOccupied] = Integer.parseInt(tokens[1]);
            currentlyOccupied ++;
        }
    }
    public static void addNewPointAndRedraw(int x, int y, String name){
        FileWriter writer;
        try {
            String line = x +" " + y + "\n";
            Files.write(Paths.get(name), line.getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }
    }

    public static void main(String[] args) {
        map = new voronoi_main();
        map.setVisible(true);
        map.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
            @Override
            public void mousePressed(MouseEvent e) {
                int x=e.getX() * size / compressed_size;
                int y=e.getY() * size / compressed_size;
                cells++;
                addNewPointAndRedraw(x,y,"./src/dataPoints");
                map.setVisible(false);
                map = new voronoi_main();
                map.setVisible(true);
                map.addMouseListener(this);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }
}
