package br.com.validadelobopro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class CatalogProfileUtilsTest {
    @Test
    public void defaultsToNewBusinessAndUsesProfileSpecificStorageKeys() {
        assertEquals("new_business", CatalogProfileUtils.defaultProfileKey());
        assertEquals("p2", CatalogProfileUtils.normalizeProfileKey("P2"));
        assertEquals("catalog_json_p2", CatalogProfileUtils.catalogJsonKey("p2"));
        assertEquals("catalog_url_p2", CatalogProfileUtils.catalogUrlKey("p2"));
    }

    @Test
    public void exposesProfileOptionsIncludingNewBusinessOption() {
        List<String> options = CatalogProfileUtils.profileLabels();
        assertTrue(options.contains("P1"));
        assertTrue(options.contains("P2"));
        assertTrue(options.contains("P6"));
        assertTrue(options.contains("P7"));
        assertTrue(options.contains("Padaria"));
    }
}

