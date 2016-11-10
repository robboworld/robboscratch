package scratchduino.robot.logs;

import java.io.*;
import java.util.*;



/**
 * Cute Table Formatter.
 *
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * History:
 *  3.2 Cell Width, row numbers
 *  3.1 Empty cell right border bugfix
 *  3.0 No-border mode has been added.
 *  2.4 Fixed the bug with non finished rows without br/separator
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2008,2009,2010,2011,2012
 * </p>
 *
 * <p>
 * </p>
 *
 * @author Alexey Zemskov
 * @version 3.2
 */
public class LogTableFormatter{

   // All text
   private LinkedList<TableRow> listTable;

   // The widthes of rows.
   private List<Integer> listWidths;

   // The space aroung twext in a cell
   private int iPadding;

   private boolean bPrintBorder;





   /**
    * This class wraps table data into nice align tables.
    *
    */
   public LogTableFormatter(){
      this.listTable = new LinkedList<TableRow>();

      this.listWidths = new ArrayList<Integer>();
      this.iPadding = 0;
      this.bPrintBorder = true;
   }





   public LogTableFormatter(int iPadding, boolean bPrintBorder){
      this();
      this.iPadding = iPadding;
      this.bPrintBorder = bPrintBorder;
   }





   public void newSeparator(){
      this.listTable.add(new RowSeparator());
   }





   public void br(){
      this.listTable.add(new RowEnd());
   }





   public void addCell(Number numCell){
      addCell("" + numCell);
   }





   public void addCell(Object cell){
      addCell("" + cell);
   }
   public void addCell(int iWidth, Object sCell){
      addCell(iWidth, "" + sCell);
   }





   public void addCell(String sCell){
      String sNullPointerProtectedCell = "" + sCell;
      addCell(sNullPointerProtectedCell.length(), sNullPointerProtectedCell);
   }



   public void addCell(int iWidth, String sCell){
      if(this.listTable.isEmpty() || (!(this.listTable.getLast() instanceof RowData))){
         this.listTable.add(new RowData());
      }

      RowData rowData = (RowData) this.listTable.getLast();

      String sNullPointerProtectedCell = "" + sCell;

      int iNullPointerProtectedCellLength = sNullPointerProtectedCell.length();
      if(iWidth < iNullPointerProtectedCellLength){
         iWidth = iNullPointerProtectedCellLength;
      }


      rowData.addCell(sNullPointerProtectedCell);

      if(listWidths.size() < rowData.listCells.size()){
         listWidths.add(iWidth);
      }
      else{
         Integer intWidth = listWidths.get(rowData.listCells.size() - 1);

         if(intWidth < iWidth){
            // The cell is narrower.
            listWidths.set(rowData.listCells.size() - 1, iWidth);
         }
         else{
            // The cell is widther.
            // Nothing to do.
         }
      }
   }





   public void addCellGroup(List<String> sRow){
      for(String sCell : sRow){
         this.addCell(sCell);
      }
   }





   public int rowCount(){
      return listTable.size();
   }





   public String toString(){
      StringWriter swResult = new StringWriter();

      String sBorder = this.getBorder();
      String sPadding = this.getPadding();

      if(this.bPrintBorder){
         swResult.write(sBorder + "\n");
      }

      for(TableRow tableRow : this.listTable){

         // data
         if(tableRow instanceof RowData){
            RowData rowData = (RowData) tableRow;

            if(this.bPrintBorder){
               swResult.write("|");
            }

            for(ListIterator<String> it = rowData.listCells.listIterator(); it.hasNext();){
               String sCell = it.next();
               swResult.write(sPadding + sCell + sPadding);

               swResult.write(getTab(this.listWidths.get(it.nextIndex() - 1) - sCell.length()));
               if(this.bPrintBorder){
                  swResult.write("|");
               }
            }

            //Empty tail of not filled cells
            //Row <1> <2> <3>
            //<1> is filled
            //Let's paing <2> <3> as empty
            for(int f = rowData.listCells.size(); f < this.listWidths.size(); f++){
               swResult.write(getTab(this.listWidths.get(f) + this.iPadding * 2));
               if(this.bPrintBorder){
                  swResult.write("|");
               }
            }
            swResult.write("\n");
         }

         // separator
         else if(tableRow instanceof RowSeparator){
            if(this.bPrintBorder){
               swResult.write(sBorder + "\n");
            }
         }
      }

      if(this.bPrintBorder){
         swResult.write(sBorder);
      }

      return swResult.toString();
   }





   private String getBorder(){
      StringWriter swResult = new StringWriter();

      swResult.write("+");
      for(Integer intWidth : this.listWidths){
         for(int f = 0; f < intWidth + this.iPadding * 2; f++){
            swResult.write("-");
         }
         swResult.write("+");
      }

      return swResult.toString();
   }





   private String getPadding(){
      return this.getTab(this.iPadding);
   }





   private String getTab(int size){
      StringWriter swResult = new StringWriter();

      for(int f = 0; f < size; f++){
         swResult.write(" ");
      }

      return swResult.toString();
   }

   private interface TableRow{
   }

   private class RowData implements TableRow{
      public List<String> listCells = new LinkedList<String>();





      public void addCell(String sData){
         listCells.add(sData);
      }
   }

   private class RowSeparator implements TableRow{
   }

   private class RowEnd implements TableRow{
   }

}
