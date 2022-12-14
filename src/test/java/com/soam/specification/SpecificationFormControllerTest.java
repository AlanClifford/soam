package com.soam.specification;

import com.soam.Util;
import com.soam.model.priority.PriorityRepository;
import com.soam.model.priority.PriorityType;
import com.soam.model.specification.Specification;
import com.soam.model.specification.SpecificationRepository;
import com.soam.model.specification.SpecificationTemplateRepository;
import com.soam.model.stakeholder.Stakeholder;
import com.soam.web.specification.SpecificationFormController;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpecificationFormController.class)
public class SpecificationFormControllerTest {

    private static Specification TEST_SPECIFICATION_1 = new Specification();
    private static Specification TEST_SPECIFICATION_2 = new Specification();
    private static Specification TEST_SPECIFICATION_3 = new Specification();

    private static final int EMPTY_SPECIFICATION_ID = 999;
    
    private static String URL_NEW_SPECIFICATION = "/specification/new";
    private static String URL_EDIT_SPECIFICATION = "/specification/{specificationId}/edit";
    private static String URL_DELETE_SPECIFICATION = "/specification/{specificationId}/delete";
    
    private static String VIEW_EDIT_SPECIFICATION =  "specification/addUpdateSpecification";



    static {

        PriorityType lowPriority = new PriorityType();
        lowPriority.setName("Low");
        lowPriority.setId(1);
        lowPriority.setSequence(1);

        PriorityType highPriority = new PriorityType();
        lowPriority.setName("High");
        lowPriority.setId(3);
        lowPriority.setSequence(3);


        TEST_SPECIFICATION_1.setId(100);
        TEST_SPECIFICATION_1.setName("Test Spec 1");
        TEST_SPECIFICATION_1.setDescription("desc");
        TEST_SPECIFICATION_1.setNotes("notes");
        TEST_SPECIFICATION_1.setPriority(lowPriority);
        TEST_SPECIFICATION_1.setStakeholders(new ArrayList<>());

        TEST_SPECIFICATION_2.setId(200);
        TEST_SPECIFICATION_2.setName("Test Spec 2");
        TEST_SPECIFICATION_2.setDescription("desc");
        TEST_SPECIFICATION_2.setNotes("notes");
        TEST_SPECIFICATION_2.setPriority(highPriority);

        Stakeholder testStakeholder = new Stakeholder();
        TEST_SPECIFICATION_2.setStakeholders(Lists.newArrayList(testStakeholder));

        TEST_SPECIFICATION_3.setId(300);
        TEST_SPECIFICATION_3.setName("Spec 3");
        TEST_SPECIFICATION_3.setDescription("desc");
        TEST_SPECIFICATION_3.setNotes("notes");
        TEST_SPECIFICATION_3.setPriority(lowPriority);
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
        given( this.specifications.findByName(TEST_SPECIFICATION_1.getName())).willReturn(Optional.of(TEST_SPECIFICATION_1));
        given( this.specifications.findByNameIgnoreCase(TEST_SPECIFICATION_1.getName())).willReturn(Optional.of(TEST_SPECIFICATION_1));
        given( this.specifications.findById(TEST_SPECIFICATION_1.getId())).willReturn(Optional.of(TEST_SPECIFICATION_1));

        given( this.specifications.findByName(TEST_SPECIFICATION_2.getName())).willReturn(Optional.of(TEST_SPECIFICATION_2));
        given( this.specifications.findByNameIgnoreCase(TEST_SPECIFICATION_2.getName())).willReturn(Optional.of(TEST_SPECIFICATION_2));
        given( this.specifications.findById(TEST_SPECIFICATION_2.getId())).willReturn(Optional.of(TEST_SPECIFICATION_2));

        given( this.specifications.findByName(TEST_SPECIFICATION_3.getName())).willReturn(Optional.of(TEST_SPECIFICATION_3));
        given( this.specifications.findByNameIgnoreCase(TEST_SPECIFICATION_3.getName())).willReturn(Optional.of(TEST_SPECIFICATION_3));
        given( this.specifications.findById(TEST_SPECIFICATION_3.getId())).willReturn(Optional.of(TEST_SPECIFICATION_3));


        given( this.specifications.findById(EMPTY_SPECIFICATION_ID)).willReturn(Optional.empty());
        

        given( this.specifications.findByNameStartsWithIgnoreCase(eq("Test"), any(Pageable.class)))
                .willReturn(new PageImpl<>(Lists.newArrayList(TEST_SPECIFICATION_2, TEST_SPECIFICATION_2)));

        given( this.specifications.findByNameStartsWithIgnoreCase(eq("Spec"), any(Pageable.class)))
                .willReturn(new PageImpl<>(Lists.newArrayList(TEST_SPECIFICATION_3)));

    }

    @Test
    void testInitCreationForm() throws Exception {
        mockMvc.perform(get(URL_NEW_SPECIFICATION)).andExpect(status().isOk())
                .andExpect(model().attributeExists("specification"))
                .andExpect(model().attributeExists("priorities"))
                .andExpect(model().attributeExists("specificationTemplates"))
                .andExpect(view().name(VIEW_EDIT_SPECIFICATION));
    }

    @Test
    void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post(URL_NEW_SPECIFICATION).param("name", "New spec")
                        .param("notes", "spec notes").param("description", "Description"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post(URL_NEW_SPECIFICATION).param("name", TEST_SPECIFICATION_1.getName())
                        .param("notes", "spec notes").param("description", "Description"))
                        .andExpect(model().attributeHasErrors("specification"))
                          .andExpect(model().attributeHasFieldErrors("specification", "name"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_EDIT_SPECIFICATION));

        mockMvc.perform(post(URL_NEW_SPECIFICATION).param("name", "New spec")
                        .param("notes", "spec notes").param("description", ""))
                .andExpect(model().attributeHasErrors("specification"))
                .andExpect(model().attributeHasFieldErrorCode("specification", "description", "NotBlank"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_EDIT_SPECIFICATION));
    }
    @Test
    void testInitUpdateSpecificationForm() throws Exception {
        Mockito.when(this.specifications.findById(TEST_SPECIFICATION_1.getId())).thenReturn(Optional.of(TEST_SPECIFICATION_1));

        mockMvc.perform(get(URL_EDIT_SPECIFICATION, TEST_SPECIFICATION_1.getId())).andExpect(status().isOk())
                .andExpect(model().attributeExists("specification"))
                .andExpect(model().attribute("specification", hasProperty("name", is(TEST_SPECIFICATION_1.getName()))))
                .andExpect(model().attribute("specification", hasProperty("description", is(TEST_SPECIFICATION_1.getDescription()))))
                .andExpect(model().attribute("specification", hasProperty("notes", is(TEST_SPECIFICATION_1.getNotes()))))
                .andExpect(view().name(VIEW_EDIT_SPECIFICATION));

        mockMvc.perform(get(URL_EDIT_SPECIFICATION, EMPTY_SPECIFICATION_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/specification/find"));
    }

    @Test
    void testProcessUpdateSpecificationFormSuccess() throws Exception {
        mockMvc.perform(post(URL_EDIT_SPECIFICATION, TEST_SPECIFICATION_1.getId())
                        .param("name", "New Test Specification")
                        .param("notes", "notes here")
                        .param("description", "description there")
                        )
                    .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/specification/{specificationId}"));
        
    }

    @Test
    void testProcessUpdateSpecificationFormHasErrors() throws Exception {
        mockMvc.perform(post(URL_EDIT_SPECIFICATION, TEST_SPECIFICATION_1.getId())
                        .param("name", "New Test Specification")
                        .param("notes", "notes")
                        .param("description", "")
                ).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("specification"))
                .andExpect(model().attributeHasFieldErrors("specification", "description"))
                .andExpect(view().name(VIEW_EDIT_SPECIFICATION));

        mockMvc.perform(post(URL_EDIT_SPECIFICATION, TEST_SPECIFICATION_1.getId())
                        .param("name", TEST_SPECIFICATION_1.getName() )
                        .param("notes", "notes")
                        .param("description", "")
                ).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("specification"))
                .andExpect(model().attributeHasFieldErrors("specification", "description"))
                .andExpect(view().name(VIEW_EDIT_SPECIFICATION));

        mockMvc.perform(post(URL_EDIT_SPECIFICATION, EMPTY_SPECIFICATION_ID)
                        .param("name", TEST_SPECIFICATION_1.getName() )
                        .param("notes", "notes")
                        .param("description", "descr")
                ).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("specification"))
                .andExpect(model().attributeHasFieldErrors("specification", "name"))
                .andExpect(view().name(VIEW_EDIT_SPECIFICATION));
    }

    @Test
    void testProcessDeleteSpecificationSuccess() throws Exception {
        mockMvc.perform(post(URL_DELETE_SPECIFICATION, TEST_SPECIFICATION_1.getId())
                        .param("name",TEST_SPECIFICATION_1.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(Util.SUB_FLASH))
                .andExpect(view().name("redirect:/specification/list"));

        mockMvc.perform(post(URL_DELETE_SPECIFICATION, TEST_SPECIFICATION_3.getId())
                        .param("name",TEST_SPECIFICATION_3.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists(Util.SUB_FLASH))
                .andExpect(view().name("redirect:/specification/list"));

    }

    @Test
    void testProcessDeleteSpecificationError() throws Exception {
        mockMvc.perform(post(URL_DELETE_SPECIFICATION, EMPTY_SPECIFICATION_ID)
                        .param("name",TEST_SPECIFICATION_1.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect( flash().attributeExists(Util.DANGER))
                .andExpect(view().name("redirect:/specification/list"));


        mockMvc.perform(post(URL_DELETE_SPECIFICATION, TEST_SPECIFICATION_2.getId())
                        .param("name",TEST_SPECIFICATION_2.getName()))
                .andExpect( flash().attributeExists(Util.SUB_FLASH))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(String.format("redirect:/specification/%s", TEST_SPECIFICATION_2.getId())));



    }
}
