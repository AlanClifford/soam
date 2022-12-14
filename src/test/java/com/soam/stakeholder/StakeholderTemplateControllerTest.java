package com.soam.stakeholder;

import com.soam.model.priority.PriorityRepository;
import com.soam.model.priority.PriorityType;
import com.soam.model.stakeholder.StakeholderTemplate;
import com.soam.model.stakeholder.StakeholderTemplateRepository;
import com.soam.web.stakeholder.StakeholderTemplateController;
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

@WebMvcTest(StakeholderTemplateController.class)
public class StakeholderTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StakeholderTemplateRepository stakeholderTemplates;

    @MockBean
    private PriorityRepository priorities;

    private static StakeholderTemplate TEST_STAKEHOLDER_1 = new StakeholderTemplate();
    private static final int EMPTY_STAKEHOLDER_ID = 999;

    private static String URL_NEW_STAKEHOLDER = "/stakeholder/new";
    private static String URL_EDIT_STAKEHOLDER = "/stakeholder/{stakeholderId}/edit";
    private static String URL_DELETE_STAKEHOLDER = "/stakeholder/{stakeholderId}/delete";
    
    private static String VIEW_FIND_STAKEHOLDER_TEMPLATE = "stakeholder/template/findStakeholderTemplate";


    static {

        PriorityType lowPriority = new PriorityType();
        lowPriority.setName("Low");
        lowPriority.setId(1);
        lowPriority.setSequence(1);


        TEST_STAKEHOLDER_1.setId(100);
        TEST_STAKEHOLDER_1.setName("Test Spec 1");
        TEST_STAKEHOLDER_1.setDescription("desc");
        TEST_STAKEHOLDER_1.setNotes("notes");
        TEST_STAKEHOLDER_1.setPriority(lowPriority);
    }



    @BeforeEach
    void setup() {

        given( this.stakeholderTemplates.findByName(TEST_STAKEHOLDER_1.getName())).willReturn(Optional.of(TEST_STAKEHOLDER_1));
        given( this.stakeholderTemplates.findByNameIgnoreCase("Test Spec")).willReturn(Optional.of(TEST_STAKEHOLDER_1));
        given( this.stakeholderTemplates.findById(TEST_STAKEHOLDER_1.getId())).willReturn(Optional.of(TEST_STAKEHOLDER_1));
        given( this.stakeholderTemplates.findById(EMPTY_STAKEHOLDER_ID)).willReturn(Optional.empty());

    }

    @Test
    void tesInitFind() throws Exception {
        mockMvc.perform(get("/stakeholder/template/find")).andExpect(status().isOk())
                .andExpect(view().name(VIEW_FIND_STAKEHOLDER_TEMPLATE));
    }

    @Test
    void testProcessFindFormSuccess() throws Exception {
        Page<StakeholderTemplate> stakeholderTemplates = new PageImpl<>(Lists.newArrayList(TEST_STAKEHOLDER_1, new StakeholderTemplate()));
        Mockito.when(this.stakeholderTemplates.findByNameStartsWithIgnoreCase(anyString(), any(Pageable.class))).thenReturn(stakeholderTemplates);
        mockMvc.perform(get("/stakeholder/templates?page=1").param("name", "Te"))
                .andExpect(status().isOk())
                .andExpect(view().name("stakeholder/template/stakeholderTemplateList"));
    }

    @Test
    void testProcessFindFormByName() throws Exception {
        Page<StakeholderTemplate> stakeholders = new PageImpl<>(Lists.newArrayList(TEST_STAKEHOLDER_1));
        //todo: Use constant for "Test"
        Mockito.when(this.stakeholderTemplates.findByNameStartsWithIgnoreCase(eq("Test"), any(Pageable.class))).thenReturn(stakeholders);
        Mockito.when(this.stakeholderTemplates.findByNameStartsWithIgnoreCase(eq("Not Present"), any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));

        mockMvc.perform(get("/stakeholder/templates?page=1").param("name", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/stakeholder/template/" + TEST_STAKEHOLDER_1.getId()+"/edit"));

        mockMvc.perform(get("/stakeholder/templates?page=1").param("name", "Not Present"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_FIND_STAKEHOLDER_TEMPLATE));

        mockMvc.perform(get("/stakeholder/templates?page=1").param("name", ""))
                .andExpect(model().attributeHasErrors("stakeholderTemplate"))
                .andExpect(model().attributeHasFieldErrors("stakeholderTemplate", "name"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_FIND_STAKEHOLDER_TEMPLATE));
    }


    @Test
    void testListStakeholderTemplates() throws Exception{
        Page<StakeholderTemplate> stakeholderTemplatesPage = new PageImpl<>(Lists.newArrayList(TEST_STAKEHOLDER_1));
        Mockito.when(this.stakeholderTemplates.findByNameStartsWithIgnoreCase(any(String.class), any(Pageable.class))).thenReturn(stakeholderTemplatesPage);

        mockMvc.perform( get("/stakeholder/template/list"))
                .andExpect(model().attributeExists("listStakeholderTemplates"))
                .andExpect(status().isOk())
                .andExpect(view().name("stakeholder/template/stakeholderTemplateList"));
    }

}
