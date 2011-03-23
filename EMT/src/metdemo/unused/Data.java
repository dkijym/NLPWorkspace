// Jon Sajdak



import java.io.*;

import java.util.*;



// container interface to data structure

public class Data
{

  private int linesMax = 16384;

  private int fieldsMax = 32;

  private String data[][];

  private String fields[];





  // Constructors

  Data()
  {

    data = new String[linesMax][fieldsMax];

    fields = new String[fieldsMax];

  }

  Data(int y, int x)
  {

    data = new String[y][x];

    fields = new String[x];

    linesMax = y;

    fieldsMax = x;

  }



  // Accessors

  // get a 2D array of all the data

  public String[][] getData()
  {

    return data;

  }



  // get an array of all the field names

  public String[] getFields()
  {

    return fields;

  }



  // retrieve value from data holder

  public String getValue(int y, int x)
  {

    if(x < getFieldsMax() && y < getLinesMax())
    {

      try
      {

        return data[y][x];

      }
      catch(ArrayIndexOutOfBoundsException e)
      {

        System.err.println("out of bounds: " + y + "," + x);

        System.exit(1);

        return "NULL";

      }

    }
    else
    {

      return "NULL";

    }

  }



  // retrieve field from field holder

  public String getField(int x)
  {

    return fields[x];

  }



  public void setField(int x, String value)
  {

    if(x < getFieldsMax())
    {

      fields[x] = value;

    }

  }



  public int getLinesMax()
  {
    return linesMax;
  }

  public int getFieldsMax()
  {
    return fieldsMax;
  }



  public void setLinesMax(int x)
  {
    linesMax = x;
  }

  public void setFieldsMax(int x)
  {
    fieldsMax = x;
  }



  // methods



  // store value in data holder

  public void storeValue(int y, int x, String value)
  {

    if(y < linesMax && x < fieldsMax)
    {

      data[y][x] = value;

    }
    else
    {

      System.err.println("storeValue out of range");

      System.exit(1);

    }

  }





  // compress data storage area to fit data it holds

  // resize, and eliminate null rows/cols

  // should only be used after an initial parse, since

  // it also handles putting first line into field name container

  public void compress()
  {

    int lineNum = 0;

    int fieldNum = 0;

    while(getValue(lineNum,0) != null)
    {

      lineNum++;

    }

    while(getValue(0,fieldNum) != null)
    {

      fieldNum++;

    }

    String tmp[][] = new String[lineNum-1][fieldNum];

    String tmp2[] = new String[fieldNum];



    for(int x = 0; x < fieldNum; x++)
    {

      tmp2[x] = getValue(0,x);

    } // end for

    for(int y = 1; y < lineNum; y++)
    {

      for(int x = 0; x < fieldNum; x++)
      {

        tmp[y-1][x] = getValue(y,x);

      } // end for

    } // end for



    // resize containers


    data = new String[lineNum-1][fieldNum];

    fields = new String[fieldNum];

    data = tmp;

    fields = tmp2;



    // reset values

    setLinesMax(lineNum-1);

    setFieldsMax(fieldNum);

  } // end compress



  // vertically compresses 2D string container and returns it

  public Data compress(Data myData)
  {

    int lineNum = 0;

    while(myData.getValue(lineNum,0) != null)
    {

      lineNum++;

    }

    Data tmp = new Data(lineNum,fieldsMax);

    tmp.fieldCopy(this);

    for(int y = 0; y < lineNum; y++)
    {

      for(int x = 0; x < myData.getFieldsMax(); x++)
      {

        tmp.storeValue(y,x,myData.getValue(y,x));

      } // end for

    } // end for


    tmp.setLinesMax(lineNum);

    tmp.setFieldsMax(fieldsMax);

    return tmp;

  } // end compress





  // print all lines where field x matches 'match' exactly

  public Data fieldMatch(int field, String match)
  {

    Data tmp = new Data(getLinesMax(),getFieldsMax());

    int lineNum = 0;

    int fieldNum = 0;

    if(field < getFieldsMax())
    {

      tmp.fieldCopy(this);

      for(int y = 0; y < getLinesMax(); y++)
      {

        if(getValue(y,0) != null)
        {

          if(match.equals(getValue(y,field)))
          {

            fieldNum = 0;

            for(int x = 0; x < getFieldsMax(); x++)
            {

              tmp.storeValue(lineNum,fieldNum++,getValue(y,x));

            } // end for

            lineNum++;

          } // end if

        } // end if

      } // end for(int y

    } // end if


    return compress(tmp);

  } // end printFieldMatch





  // print all lines where field x matches 'match' exactly

  public Data fieldMatch(int field, String match, Data myData)
  {

    return(myData.fieldMatch(field,match));

  } // end fieldMatch





  // copy field names from this guy

  public void fieldCopy(Data myData)
  {

    for(int x = 0; x < myData.getFieldsMax(); x++)
    {

      setField(x,myData.getField(x));

    } // end for

    setFieldsMax(myData.getFieldsMax());

  } // end fieldCopy





  // just print all the data

  public void print()
  {

    int x = 0;

    int y = 0;

    Object hashes[] = new Object[getFieldsMax()];

    for(int i = 0; i < getFieldsMax(); i++)
    {

      hashes[i] = (Object)new HashMap();

    }



    // print default fieldnames

    for(x = 0; x < getFieldsMax(); x++)
    {

      System.out.print(getField(x) + "\t");

    } // end for

    System.out.print("\n");



    // print 2d array

    for(y = 0; y < getLinesMax(); y++)
    {

      for(x = 0; x < getFieldsMax(); x++)
      {

        if(((HashMap)hashes[x]).get(getValue(y,x)) == null)
        {

          ((HashMap)hashes[x]).put(getValue(y,x), "1");

          System.out.print( getValue(y,x) + " \t ");

        }
        else
        {

          System.out.print( getValue(y,x) + "*\t ");

        }

      } // end for

      System.out.print("\n");

    } // end for

  } // end print





  // print any 2D data holder

  // prints headers first


  public void print(Data myData)
  {

    myData.print();

  }





  /////////////// metrics //////



  // Prevalence = number of cases of specific virus file, id'ed by an md5

  public int metricPrevalence(String md5)
  {

    Data tmp = fieldMatch(0,md5);

    return(tmp.getLinesMax());

  }



  // Spread = number of unique mail servers a virus has spread to

  public int metricSpread(String md5)
  {

    Data tmp = fieldMatch(0,md5);

    return(tmp.uniqueValuesInField(1));

  }



  // Threat = (spread + prevalence)/

  //	(total # of mail servers on network + total # of viruses)

  public float metricThreat(String md5)
  {

    int s = metricSpread(md5);

    int p = metricPrevalence(md5);

    int m = uniqueValuesInField(1);

    int v = uniqueValuesInField(0);

    return( (float)(s+p) / (float)(m+v) );

  }



  // number of unique values in a given field

  public int uniqueValuesInField(int field)
  {

    HashMap hash = new HashMap();

    int count = 0;

    if(field < getFieldsMax())
    {

      for(int y = 0; y < getLinesMax(); y++)
      {

        if(hash.get(getValue(y,field)) == null)
        {

          hash.put(getValue(y,field), "1");

          count++;

        }

      }

      return count;

    }

    return 1;

  }



  // Cost = average downtime, average recovert time, average monetary cost

  //	incurred during infection by a virus (we need outside information

  //  for this metric so we'll worry about it later)

  public float metricCost(String md5)
  {

    int s = metricSpread(md5);

    int p = metricPrevalence(md5);

    int m = uniqueValuesInField(1);

    int v = uniqueValuesInField(0);

    float cost = (float)s * (float)p * (float)metricThreat(md5);

    return (float)cost;

  }



}



