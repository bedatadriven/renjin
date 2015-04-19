package org.renjin.utils.table;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class TypeDetectorTest {

    @Test
    public void integers() {
        TypeDetector typeDetector = new TypeDetector(new TableOptions());
        TypeDetector.Buffer buffer = typeDetector.newBuffer();
        buffer.update("12323");
        buffer.update("02500");
        
        assertThat(buffer.getType(), equalTo(TypeDetector.INTEGER));
    }

    @Test
    public void character() {
        TypeDetector typeDetector = new TypeDetector(new TableOptions());
        TypeDetector.Buffer buffer = typeDetector.newBuffer();
        buffer.update("12323");
        buffer.update("abdc");

        assertThat(buffer.getType(), equalTo(TypeDetector.CHARACTER));
    }


    @Test
    public void doubleType() {
        TypeDetector typeDetector = new TypeDetector(new TableOptions());
        TypeDetector.Buffer buffer = typeDetector.newBuffer();
        buffer.update("12.323");
        buffer.update("14");

        assertThat(buffer.getType(), equalTo(TypeDetector.DOUBLE));
        
        buffer.update("ab");
        assertThat(buffer.getType(), equalTo(TypeDetector.CHARACTER));
    }



}