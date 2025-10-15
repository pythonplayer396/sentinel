# Contributing to Sentinel

Thank you for your interest in contributing to Sentinel! This document provides guidelines for contributing.

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors.

### Our Standards

- Be respectful and professional
- Focus on constructive feedback
- Accept responsibility for mistakes
- Prioritize security and safety

## How to Contribute

### Reporting Bugs

1. Check existing issues first
2. Use the bug report template
3. Include reproduction steps
4. Provide system information

### Suggesting Features

1. Check existing feature requests
2. Explain the use case clearly
3. Consider security implications
4. Propose implementation approach

### Contributing Code

1. Fork the repository
2. Create a feature branch
3. Write tests for new code
4. Follow coding standards
5. Submit a pull request

## Development Setup

```bash
# Clone repository
git clone https://github.com/yourusername/sentinel.git
cd sentinel

# Build project
mvn clean package

# Run tests
mvn test

# Start test lab
docker-compose up -d
```

## Coding Standards

### Java Style

- Use Java 17 features
- Follow standard Java naming conventions
- Maximum line length: 120 characters
- Use meaningful variable names

### Documentation

- JavaDoc for all public APIs
- Comments for complex logic
- Update README for new features
- Include usage examples

### Testing

- Unit tests for all new code
- Integration tests for features
- Maintain >80% code coverage
- Test edge cases

## Plugin Development

### Safety Requirements

All plugins must:
- Be non-destructive
- Not exploit vulnerabilities
- Classify safety level accurately
- Handle errors gracefully

### Plugin Checklist

- [ ] Implements ScannerPlugin interface
- [ ] Includes comprehensive tests
- [ ] Documents detection logic
- [ ] Provides remediation guidance
- [ ] Minimizes false positives

## Pull Request Process

### Before Submitting

1. Update documentation
2. Add/update tests
3. Run full test suite
4. Check code style
5. Update CHANGELOG

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation

## Testing
How was this tested?

## Checklist
- [ ] Tests pass
- [ ] Documentation updated
- [ ] Code style followed
- [ ] No security issues
```

### Review Process

1. Automated checks must pass
2. At least one maintainer approval
3. No unresolved discussions
4. Documentation complete

## Security

### Reporting Vulnerabilities

**DO NOT** open public issues for security vulnerabilities.

Email: security@sentinel-scanner.org

Include:
- Description of vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

### Security Guidelines

- Never commit credentials
- Validate all inputs
- Use parameterized queries
- Follow OWASP guidelines
- Implement rate limiting

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Questions?

- GitHub Discussions
- Issue Tracker
- Email: dev@sentinel-scanner.org

## Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md
- Release notes
- Project documentation

Thank you for contributing to Sentinel!
