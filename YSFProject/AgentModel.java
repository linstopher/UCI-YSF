import java.util.Scanner;
import java.io.*;
/**
 * Creates an agent-based model, either perfect-mixing or spatial
 * 
 * @author Chris Lin
 * @version 6/29/15
 */
public class AgentModel
{
    private int[][] grid;
    private int x, y;
    private int numStartCells;
    
    private double probDeath;
    private double probDiv;
    private double probDifferentiate;
    private double probEvolve;
    
    private boolean perfectMixing;
    private boolean differentiating;
    private boolean evolving;
    
    private String data;
    
    public AgentModel(int gridX, int gridY, int numSC, boolean pM, boolean diff, boolean evolve)
    {
        x = gridX;
        y = gridY; //y length != x length if not using a square grid
        grid = new int[x][y];
        numStartCells = numSC;
        
        probDeath = 0.01; //probability of cell dying (0.01 = 1% chance of death)
        probDiv = 0.75; //probability of cell dividing (0.97 = 97% chance of div)
        probDifferentiate = 0.25; //probability of cell differentiating (0.4 = 40% chance of diff)
        probEvolve = 0.000; //probability of diff cell evolving (0.01 = 1% chance of mut)
            //0.001
        perfectMixing = pM;
        differentiating = diff;
        evolving = evolve;
        
        data = "";
        
        createNewRoot(numStartCells);
    }
    
    public static void main(/*int gridSize, int numStartCells,*/ boolean perfectMixing, boolean differentiating)//, boolean evolving)
    {
        int gridSize = 100;
        AgentModel test = new AgentModel(/*gridSize, gridSize, numStartCells,*/100, 100, 5, perfectMixing, differentiating, false);//evolving);
        if (perfectMixing)
        {
            System.out.println("PERFECT MIXING SIMULATION\n");
        }
        else
        {
            System.out.println("SPATIAL SIMULATION\n");
        }
        
        for (int i = 0; i <= 200; i++)
        {
            test.updateStage(i);
        }
        
        System.out.println("Save data? (Y/N)");
            Scanner kboard = new Scanner(System.in);
            boolean done = false;
            String answer = "";
            while (!done)
            {
                String input = kboard.next().toString().toLowerCase();
                if (input.equals("y") || input.equals("yes") || input.equals("n") || input.equals("no"))
                {
                    answer = input;
                    done = true;
                }
                else
                {
                    System.out.println("Invalid input, try again.");
                }
            }
            
            if (answer.equals("y") || answer.equals("yes"))
            {
                String filename = "TumorModel-" ;
                    if (differentiating)
                        filename += "diff-";
                    else
                        filename += "undiff-";
                
                    if (perfectMixing)
                        filename += "PM";
                    else
                        filename += "S";
                    filename += ("-" + gridSize);
                    //filename += System.currentTimeMillis();
                    //filename += "_" + test.probDeath;
                    //filename += "_" + test.probDiv;
                test.saveData(filename);
                System.out.println("Data saved to file named: " + filename);
            }
            else
            {
                System.out.println("Data deleted.");
            }
    }
    
    private void saveData(String filename)
    {
        try
        {
            FileWriter fw = new FileWriter(filename + ".txt");
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write("#StageNum, CellCount, Sqrt/log, StemCount, DiffCount, MutantCount");
            writer.write(getData());
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("Saving failed.");
        }
    }
    
    private String getData()
    {
        return data;
    }
    
    private void createNewRoot(int numCells)
    {
        int randX = x/2;
        int randY = x/2;
        if (perfectMixing)
        {
            for (int i = 0; i < numCells; i++)
            {
                randX = (int) (Math.random() * x);
                randY = (int) (Math.random() * y);
                
                grid[randX][randY] = 1;
            }
        }
        else
        {
            int diameter = (int)(Math.sqrt(numCells));
            int startCol, endCol, startRow, endRow;
            if (diameter != 1)
            {
                startCol = (int)((x/2.0) - ((diameter - 1)/2.0));
                endCol = (int)((x/2.0) + ((diameter - 1)/2.0));
        
                startRow = (int)((y/2.0) - ((diameter - 1)/2.0));
                endRow = (int)((y/2.0) + ((diameter - 1)/2.0));
            }
            else
            {
                startCol = (int)(x/2.0);
                endCol = startCol;
           
                startRow = (int)(y/2.0);
                endRow = startRow;
            }
        
            for (int c = startCol; c <= endCol; c++)
            {
                for (int r = startRow; r <= endRow; r++)
                {
                    grid[r][c] = 1;
                }
            
            }
            
            int lR = startRow - 1;
            int lC = startCol;
            boolean lSF = false; //left side filled
            while (numCells - getTotalCancerCells() > 0)
            {
                grid[lR][lC] = 1;
                
                if (lR == startRow -1 && lC < endCol)
                    lC++;
                else if (lC == endCol)
                {
                    lR++;
                    lC = startCol - 1;
                }
                else if (lR <= endRow)
                {
                    if (lSF)
                    {
                        lC = endCol + 1;
                        lSF = false;
                    }
                    else
                    {
                        if (lR != startRow)
                            lR++;
                        lC = startCol - 1;
                        lSF = true;
                    }
                }
                else if (lR == endRow + 1 && lC <= endCol)
                {
                    if (lC != endCol)
                        lC++;
                }
                else
                {
                    lR = startRow - 1;
                    lC = startCol - 1;
                }
            }
        }
    }
    
    private void showGrid()
    {
        for (int[] numArray : grid)
        {
            System.out.print("[");
            for (int i = 0; i < numArray.length; i++)
            {
                if (i != numArray.length-1)
                    System.out.print(numArray[i] + ", ");
                else
                    System.out.print(numArray[i]);
            }
            System.out.println("]");
        }
    }
    
    private void updateStage(int stageNum)
    {
        if (stageNum != 0)
        {
            for (int i = 0; i < x * x; i++)
            {
                int randR = (int)(Math.random() * x);
                int randC = (int)(Math.random() * x);
            
                updateSq(randR, randC);
            }
        }
        
        System.out.println("Stage " + stageNum);
            data += ("\n" + stageNum + " ");
        System.out.println("Total cells: " + getTotalCancerCells());
            data += ("" + getTotalCancerCells() + " " + getThirdColumnData() + " " + getStemCells() + " " + getDiffCells() + " " + getMutants());
        System.out.println("Mutants: " + getMutants() + "\n");
        showGrid();
        System.out.println("\n============================================");
    }
    
    private void updateSq(int r, int c)
    {
        if (grid[r][c] == 1)
        {
            double randNum = Math.random() * (probDeath + probDiv);
            if (randNum < probDeath)
            {
                grid[r][c] = 0;
            }
            else if (randNum < probDeath + probDiv)
            {
                if (perfectMixing)
                    pMDivide(r, c);
                else
                    spatialDivide(r, c);
            }
        }
        else if (grid[r][c] == 2 || grid[r][c] == 3)
        {
            double randNum = Math.random();
            if (randNum < probDeath)
                grid[r][c] = 0;
        }
    }
    
    private void spatialDivide(int rootR, int rootC)
    {
        GridCoord[] coords = new GridCoord[9]; //List of 9 coordinates around root cancer cell
        int counter = 0;
        for (int row = rootR - 1; row <= rootR + 1; row++) //Start at upper left corner
        {
            for (int col = rootC - 1; col <= rootC + 1; col++) //End at lower right corner
            {
                if (row != rootR || col != rootC)
                {
                    if (row >= 0 && row < x && col >= 0 && col < y)
                        coords[counter] = new GridCoord(row, col);
                    else
                        coords[counter] = new GridCoord(-1, -1);
                    counter++;
                }
                else
                {
                    coords[4] = new GridCoord(-1, -1);
                    counter = 5;
                }
            }
        }
        
        int randI = (int)(Math.random() * 9);
        GridCoord rand = new GridCoord(-1, -1);
        int randR = rand.getRowCoord();
        int randC = rand.getColCoord();
        
        while (randR == -1 || randC == -1)
        {
            randI = (int)(Math.random() * 8);
            rand = coords[randI];
            randR = rand.getRowCoord();
            randC = rand.getColCoord();
        }
        int xCoord = rand.getRowCoord();
        int yCoord = rand.getColCoord();
        
        double randNum = Math.random();
        if (differentiating && randNum < probDifferentiate)
        {
            double randNum2 = Math.random();
            if (evolving && randNum2 < probEvolve)
            {
                grid[xCoord][yCoord] = 3;
                grid[rootR][rootC] = 2;
            }
            else
            {
                grid[xCoord][yCoord] = 2;
                grid[rootR][rootC] = 2;
            }
        }
        else
        {
            double randNum2 = Math.random();
            if (evolving && randNum2 < probEvolve)
            {
                grid[xCoord][yCoord] = 4;
            }
            else
            {
                grid[xCoord][yCoord] = 1;
            }
        }
    }
    
    private void pMDivide(int r, int c)
    {
        int randR = (int)(Math.random() * y);
        int randC = (int)(Math.random() * x);
        
        if (grid[randR][randC] == 0)
        {
            double randNum = Math.random();
            if (differentiating && randNum < probDifferentiate)
            {
                double randNum2 = Math.random();
                if (evolving && randNum2 < probEvolve)
                {
                    grid[randR][randC] = 3;
                    grid[r][c] = 2;
                }
                else
                {
                    grid[randR][randC] = 2;
                    grid[r][c] = 2;
                }
            }
            else
            {
                double randNum2 = Math.random();
                if (evolving && randNum2 < probEvolve)
                {
                    grid[randR][randC] = 4;
                }
                else
                {
                    grid[randR][randC] = 1;
                }
            }
        }
    }
    
    private int getTotalCancerCells()
    {
        int sum = 0;
        for (int[] arr : grid)
        {
            for (int num : arr)
            {
                if (num != 0)
                {
                    sum++;
                }
            }
        }
        
        return sum;
    }
    
    private int getStemCells()
    {
        int sum = 0;
        for (int[] arr : grid)
        {
            for (int num : arr)
            {
                if (num == 1)
                {
                    sum++;
                }
            }
        }
        
        return sum;
    }
    
    private int getDiffCells()
    {
        int sum = 0;
        for (int[] arr : grid)
        {
            for (int num : arr)
            {
                if (num == 2)
                {
                    sum++;
                }
            }
        }
        
        return sum;
    }
    
    private int getMutants()
    {
        int sum = 0;
        for (int[] arr : grid)
        {
            for (int num : arr)
            {
                if (num == 3 || num == 4)
                {
                    sum++;
                }
            }
        }
        
        return sum;
    }
    
    private double getThirdColumnData()
    {
        if (perfectMixing)
        {
            return Math.log(getTotalCancerCells());
        }
        else
        {
            return Math.sqrt(getTotalCancerCells());
        }
    }
    
    private class GridCoord
    {
        private int rowNum;
        private int colNum;
        
        public GridCoord(int r, int c)
        {
            rowNum = r;
            colNum = c;
        }
        
        public int getRowCoord()
        {
            return rowNum;
        }
        public int getColCoord()
        {
            return colNum;
        }
        
        public String getString()
        {
            return "(" + rowNum + ", " + colNum + ")";
        }
    }
}
