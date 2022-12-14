package com.soam.objective;

import com.soam.model.objective.ObjectiveTemplate;
import com.soam.model.objective.ObjectiveTemplateRepository;
import com.soam.model.priority.PriorityRepository;
import com.soam.model.priority.PriorityType;
import com.soam.web.objective.ObjectiveTemplateController;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ObjectiveTemplateController.class)
public class ObjectiveTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ObjectiveTemplateRepository objectiveTemplates;

    @MockBean
    private PriorityRepository priorities;

    private static ObjectiveTemplate TEST_OBJECTIVE_1 = new ObjectiveTemplate();
    private static final int EMPTY_OBJECTIVE_ID = 999;

    private static String URL_NEW_OBJECTIVE = "/objective/new";
    private static String URL_EDIT_OBJECTIVE = "/objective/{objectiveId}/edit";
    private static String URL_DELETE_OBJECTIVE = "/objective/{objectiveId}/delete";
    
    private static String VIEW_FIND_OBJECTIVE_TEMPLATE = "objective/template/findObjectiveTemplate";


    static {

        PriorityType lowPriority = new PriorityType();
        lowPriority.setName("Low");
        lowPriority.setId(1);
        lowPriority.setSequence(1);


        TEST_OBJECTIVE_1.setId(100);
        TEST_OBJECTIVE_1.setName("Test Spec 1");
        TEST_OBJECTIVE_1.setDescription("desc");
        TEST_OBJECTIVE_1.setNotes("notes");
        TEST_OBJECTIVE_1.setPriority(lowPriority);
    }



    @BeforeEach
    void setup() {

        given( this.objectiveTemplates.findByName(TEST_OBJECTIVE_1.getName())).willReturn(Optional.of(TEST_OBJECTIVE_1));
        given( this.objectiveTemplates.findByNameIgnoreCase("Test Spec")).willReturn(Optional.of(TEST_OBJECTIVE_1));
        given( this.objectiveTemplates.findById(TEST_OBJECTIVE_1.getId())).willReturn(Optional.of(TEST_OBJECTIVE_1));
        given( this.objectiveTemplates.findById(EMPTY_OBJECTIVE_ID)).willReturn(Optional.empty());

    }

    @Test
    void tesInitFind() throws Exception {
        mockMvc.perform(get("/objective/template/find")).andExpect(status().isOk())
                .andExpect(view().name(VIEW_FIND_OBJECTIVE_TEMPLATE));
    }

    @Test
    void testProcessFindFormSuccess() throws Exception {
        Page<ObjectiveTemplate> objectiveTemplates = new PageImpl<>(Lists.newArrayList(TEST_OBJECTIVE_1, new ObjectiveTemplate()));
        Mockito.when(this.objectiveTemplates.findByNameStartsWithIgnoreCase(anyString(), any(Pageable.class))).thenReturn(objectiveTemplates);
        mockMvc.perform(get("/objective/templates?page=1").param("name", "Te"))
                .andExpect(status().isOk())
                .andExpect(view().name("objective/template/objectiveTemplateList"));
    }

    @Test
    void testProcessFindFormByName() throws Exception {
        Page<ObjectiveTemplate> objectives = new PageImpl<>(Lists.newArrayList(TEST_OBJECTIVE_1));
        //todo: Use constant for "Test"
        Mockito.when(this.objectiveTemplates.findByNameStartsWithIgnoreCase(eq("Test"), any(Pageable.class))).thenReturn(objectives);
        Mockito.when(this.objectiveTemplates.findByNameStartsWithIgnoreCase(eq("Not Present"), any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));

        mockMvc.perform(get("/objective/templates?page=1").param("name", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/objective/template/" + TEST_OBJECTIVE_1.getId()+"/edit"));

        mockMvc.perform(get("/objective/templates?page=1").param("name", "Not Present"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_FIND_OBJECTIVE_TEMPLATE));

        mockMvc.perform(get("/objective/templates?page=1").param("name", ""))
                .andExpect(model().attributeHasErrors("objectiveTemplate"))
                .andExpect(model().attributeHasFieldErrors("objectiveTemplate", "name"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_FIND_OBJECTIVE_TEMPLATE));
    }


    @Test
    void testListObjectiveTemplates() throws Exception{
        Page<ObjectiveTemplate> objectiveTemplatesPage = new PageImpl<>(Lists.newArrayList(TEST_OBJECTIVE_1));
        Mockito.when(this.objectiveTemplates.findByNameStartsWithIgnoreCase(any(String.class), any(Pageable.class))).thenReturn(objectiveTemplatesPage);

        mockMvc.perform( get("/objective/template/list"))
                .andExpect(model().attributeExists("listObjectiveTemplates"))
                .andExpect(status().isOk())
                .andExpect(view().name("objective/template/objectiveTemplateList"));
    }

}
