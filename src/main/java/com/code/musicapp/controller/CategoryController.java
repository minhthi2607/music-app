package com.code.musicapp.controller;

import com.code.musicapp.entity.Category;
import com.code.musicapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // Danh sách thể loại
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "categories/list";
    }

    // Hiển thị form thêm
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/create";
    }

    // Lưu thể loại mới
    @PostMapping("/create")
    public String createCategory(@ModelAttribute Category category) {

        categoryRepository.save(category);

        return "redirect:/categories";
    }
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {

        categoryRepository.deleteById(id);

        return "redirect:/categories";
    }
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        model.addAttribute("category", category);

        return "categories/edit";
    }
    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @ModelAttribute Category updatedCategory) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(updatedCategory.getName());

        categoryRepository.save(category);

        return "redirect:/categories";
    }
}