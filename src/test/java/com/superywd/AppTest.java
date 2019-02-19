package com.superywd;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void test1() throws Exception{
        Random random = new Random();
        while (true){
            final String abc = String.valueOf(random.nextInt());
            System.out.println(abc);
            Thread.sleep(2000);
        }
    }
}
