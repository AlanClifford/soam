package com.soam.specification;

import com.soam.Util;
import com.soam.model.priority.PriorityRepository;
import com.soam.model.priority.PriorityType;
import com.soam.model.specification.SpecificationRepository;
import com.soam.model.specification.SpecificationTemplate;
import com.soam.model.specification.SpecificationTemplateRepository;
import com.soam.web.specification.SpecificationTemplateFormController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpecificationTemplateFormController.class)
public class SpecificationTemplateFormControllerTest {

    private static final SpecificationTemplate TEST_SPECIFICATION_1 = new SpecificationTemplate();
    private static final int EMPTY_SPECIFICATION_ID = 200;

    private static final String URL_NEW_TEMPLATE =  "/specification/template/new";
    private static final String URL_EDIT_TEMPLATE =  "/specification/template/{specificationId}/edit";
    private static final String URL_DELETE_TEMPLATE =  "/specification/template/{specificationId}/delete";
    
    private static final String VIEW_ADD_UPDATE_TEMPLATE = "specification/template/addUpdateSpecificationTemplate";
    private static final String VIEW_REDIRECT_LIST_TEMPLATE = "redirect:/specification/template/list";

    static {

        PriorityType lowPriority = new PriorityType();
        lowPriority.setName("Low");
        lowPriority.setId(1);
        lowPriority.setSequence(1);


        TEST_SPECIFICATION_1.setId(100);
        TEST_SPECIFICATION_1.setName("Test Spec 1");
        TEST_SPECIFICATION_1.setDescription("desc");
        TEST_SPECIFICATION_1.setNotes("notes");
        TEST_SPECIFICATION_1.setPriority(lowPriority);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpecificationRepository specifications;

    @MockBean
    private SpecificationTemplateRepository specificationTemplates;

    @MockBean
    private PriorityRepository priorities;


    @BeforeEach
    void setup() {
        given( this.specificationTemplates.findByName(TEST_SPECIFICATION_1.getName())).willReturn(Optional.of(TEST_SPECIFICATION_1));
        given( this.specificationTemplates.findByNameIgnoreCase(TEST_SPECIFICATION_1.getName())).willReturn(Optional.of(TEST_SPECIFICATION_1));
        given( this.specificationTemplates.findById(TEST_SPECIFICATION_1.getId())).willReturn(Optional.of(TEST_SPECIFICATION_1));
        given( this.specificationTemplates.findById(EMPTY_SPECIFICATION_ID)).willReturn(Optional.empty());

    }

    @Test
    void testInitCreationForm() throws Exception {
        mockMvc.perform(get(URL_NEW_TEMPLATE)).andExpect(status().isOk())
                .andExpect(model().attributeExists("specificationTemplate"))
                .andExpect(model().attributeExists("priorities"))
                .andExpect(model().attributeExists("specificationTemplates"))
                .andExpect(view().name(VIEW_ADD_UPDATE_TEMPLATE));
    }

    @Test
    void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post(URL_NEW_TEMPLATE).param("name", "New spec")
                        .param("notes", "spec notes").param("description", "Description"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post(URL_NEW_TEMPLATE).param("name", TEST_SPECIFICATION_1.getName())
                        .param("notes", "spec notes").param("description", "Description"))
                        .andExpect(model().attributeHasErrors("specificationTemplate"))
                          .andExpect(model().attributeHasFieldErrors("specificationTemplate", "name"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_ADD_UPDATE_TEMPLATE));

        mockMvc.perform(post(URL_NEW_TEMPLATE).param("name", "New spec")
                        .param("notes", "spec notes").param("description", ""))
                .andExpect(model().attributeHasErrors("specificationTemplate"))
                .andExpect(model().attributeHasFieldErrorCode("specificationTemplate", "description", "NotBlank"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_ADD_UPDATE_TEMPLATE));
    }

    @Test
    void testInitUpdateSpecificationForm() throws Exception {
        Mockito.when(this.specificationTemplates.findById(TEST_SPECIFICATION_1.getId())).thenReturn(Optional.of(TEST_SPECIFICATION_1));

        mockMvc.perform(get(URL_EDIT_TEMPLATE, TEST_SPECIFICATION_1.getId())).andExpect(status().isOk())
                .andExpect(model().attributeExists("specificationTemplate"))
                .andExpect(model().attribute("specificationTemplate", hasProperty("name", is(TEST_SPECIFICATION_1.getName()))))
                .andExpect(model().attribute("specificationTemplate", hasProperty("description", is("desc"))))
                .andExpect(model().attribute("specificationTemplate", hasProperty("notes", is("notes"))))
                .andExpect(view().name(VIEW_ADD_UPDATE_TEMPLATE));

        mockMvc.perform(get(URL_EDIT_TEMPLATE, EMPTY_SPECIFICATION_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(VIEW_REDIRECT_LIST_TEMPLATE));
    }

    @Test
    void testProcessUpdateSpecificationFormSuccess() throws Exception {
        Mockito.when(this.specificationTemplates.findById(EMPTY_SPECIFICATION_ID)).thenReturn(Optional.empty());
        mockMvc.perform(post(URL_EDIT_TEMPLATE, TEST_SPECIFICATION_1.getId())
                        .param("name", "New Test Specification")
                        .param("notes", "notes here")
                        .param("description", "description there")
                        )
                    .andExpect(status().is3xxRedirection())
                .andExpect(view().name(VIEW_REDIRECT_LIST_TEMPLATE));


        mockMvc.perform(post(URL_EDIT_TEMPLATE, TEST_SPECIFICATION_1.getId())
                                .param("name", TEST_SPECIFICATION_1.getName())
                                .param("notes", "notes here")
                                .param("description", "description there")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(VIEW_REDIRECT_LIST_TEMPLATE));
    }

    @Test
    void testProcessUpdateOwnerFormHasErrors() throws Exception {
        mockMvc.perform(post(URL_EDIT_TEMPLATE, TEST_SPECIFICATION_1.getId())
                        .param("name", "New Test Specification")
                        .param("notes", "notes")
                        .param("description", "")
                ).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("specificationTemplate"))
                .andExpect(model().attributeHasFieldErrors("specificationTemplate", "description"))
                .andExpect(view().name(VIEW_ADD_UPDATE_TEMPLATE));

        mockMvc.perform(post(URL_EDIT_TEMPLATE, EMPTY_SPECIFICATION_ID)
                        .param("name", TEST_SPECIFICATION_1.getName())
                        .param("notes", "notes")
                        .param("description", "")
                ).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("specificationTemplate"))
                .andExpect(model().attributeHasFieldErrors("specificationTemplate", "name"))
                .andExpect(view().name(VIEW_ADD_UPDATE_TEMPLATE));
    }

    @Test
    void testProcessDeleteSpecificationSuccess() throws Exception {
        mockMvc.perform(post(URL_DELETE_TEMPLATE, TEST_SPECIFICATION_1.getId())
                        .param("name", TEST_SPECIFICATION_1.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(Util.SUB_FLASH))
                .andExpect(view().name(VIEW_REDIRECT_LIST_TEMPLATE));

    }

    @Test
    void testProcessDeleteSpecificationError() throws Exception {
        mockMvc.perform(post(URL_DELETE_TEMPLATE, EMPTY_SPECIFICATION_ID)
                        .param("name", TEST_SPECIFICATION_1.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect( flash().attributeExists(Util.DANGER))
                .andExpect(view().name(VIEW_REDIRECT_LIST_TEMPLATE));

    }
}
